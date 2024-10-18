from fastapi import FastAPI, File, UploadFile, Form, HTTPException, BackgroundTasks
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import httpx
import json
import os
import base64
import uuid
import asyncio
from typing import Dict, Any
import logging
import time

app = FastAPI()

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 로깅 설정
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# 디렉토리 설정
BASE_DIR = os.path.abspath(os.path.dirname(__file__))
COMFY_DIR = BASE_DIR
FASTAPI_DIR = os.path.join(COMFY_DIR, "fastapi")
WORKFLOW_DIR = os.path.join(FASTAPI_DIR, "workflow")
UPLOAD_DIR = os.path.join(WORKFLOW_DIR, "upload")

# ComfyUI API 엔드포인트
COMFY_API = "http://localhost:8188/api"

tasks = {}

def load_workflow() -> Dict[str, Any]:
    workflow_path = os.path.join(WORKFLOW_DIR, "test1.json")
    logger.debug(f"Attempting to load workflow from: {workflow_path}")
    if not os.path.exists(workflow_path):
        raise FileNotFoundError(f"Workflow file not found: {workflow_path}")
    try:
        with open(workflow_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except UnicodeDecodeError:
        with open(workflow_path, 'r', encoding='cp949') as f:
            return json.load(f)
    except json.JSONDecodeError:
        raise ValueError(f"Invalid JSON in file: {workflow_path}")
    except Exception as e:
        raise Exception(f"Error reading workflow file: {str(e)}")

def update_workflow(workflow: Dict[str, Any], image_path: str, prompt: str) -> Dict[str, Any]:
    workflow["216"]["inputs"]["image"] = image_path
    workflow["169"]["inputs"]["prompt"] = prompt + ', only object, simple'
    workflow["7"]["inputs"]["text"] = "easynegative, badhandv4, low quality, worst quality, nude, bad shape"
    return workflow

async def send_workflow(workflow: Dict[str, Any]) -> str:
    async with httpx.AsyncClient() as client:
        response = await client.post(f"{COMFY_API}/prompt", json={"prompt": workflow})
        if response.status_code != 200:
            raise HTTPException(status_code=500, detail="Failed to send workflow to ComfyUI")
        return response.json()['prompt_id']

async def get_image(prompt_id: str) -> str:
    start_time = time.time()
    async with httpx.AsyncClient() as client:
        for i in range(300):  # 5분으로 증가
            logger.debug(f"Attempt {i+1} to get image for prompt_id: {prompt_id}")
            response = await client.get(f"{COMFY_API}/history/{prompt_id}")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Failed to get result from ComfyUI")
            
            data = response.json()
            logger.debug(f"Received data from ComfyUI: {data}")
            if prompt_id in data and 'outputs' in data[prompt_id]:
                outputs = data[prompt_id]['outputs']
                logger.debug(f"Processing outputs: {outputs}")
                for node_id, node_output in outputs.items():
                    if 'images' in node_output:
                        images = node_output['images']
                        if images:
                            image_info = images[0]
                            if 'filename' in image_info:
                                filename = image_info['filename']
                                subfolder = image_info.get('subfolder', '')
                                return await get_image_data(filename, subfolder)
            
            elapsed_time = time.time() - start_time
            logger.debug(f"Elapsed time: {elapsed_time:.2f} seconds")
            await asyncio.sleep(2)
    
    logger.error(f"Timeout after {time.time() - start_time:.2f} seconds")
    raise HTTPException(status_code=504, detail="Timeout: Image generation took too long. Please try again.")

async def get_image_data(filename: str, subfolder: str) -> str:
    image_path = os.path.join(COMFY_DIR, 'output', subfolder, filename)
    logger.debug(f"Attempting to read image file: {image_path}")
    if not os.path.exists(image_path):
        raise HTTPException(status_code=404, detail=f"Image file not found: {image_path}")
    
    with open(image_path, "rb") as image_file:
        image_data = base64.b64encode(image_file.read()).decode('utf-8')
    
    return f"data:image/png;base64,{image_data}"

@app.post("/generate/")
async def generate(background_tasks: BackgroundTasks, file: UploadFile = File(...), request: str = Form(...)):
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
        image_path = os.path.join(UPLOAD_DIR, file.filename)
        with open(image_path, "wb") as buffer:
            content = await file.read()
            buffer.write(content)
        
        request_data = json.loads(request)
        prompt = request_data.get('prompt', '')

        task_id = str(uuid.uuid4())
        tasks[task_id] = {"status": "processing"}
        
        background_tasks.add_task(process_image, task_id, image_path, prompt)
        return JSONResponse(content={"task_id": task_id, "status": "processing"})
    except Exception as e:
        logger.error(f"Error in generate: {str(e)}")
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

@app.get("/status/{task_id}")
async def get_task_status(task_id: str):
    task = tasks.get(task_id)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    
    if task["status"] == "completed":
        image = task["image"]
        del tasks[task_id]  # 완료된 작업 제거
        return JSONResponse(content={"status": "completed", "image": image})
    elif task["status"] == "failed":
        error = task["error"]
        del tasks[task_id]  # 실패한 작업 제거
        return JSONResponse(content={"status": "failed", "error": error})
    else:
        return JSONResponse(content={"status": "processing"})

async def process_image(task_id: str, image_path: str, prompt: str):
    try:
        workflow = load_workflow()
        updated_workflow = update_workflow(workflow, image_path, prompt)
        prompt_id = await send_workflow(updated_workflow)
        tasks[task_id] = {"status": "waiting_for_comfyui", "prompt_id": prompt_id}
        
        # 별도의 태스크로 이미지 대기
        await wait_for_image(task_id, prompt_id)
    except Exception as e:
        logger.error(f"Error in process_image: {str(e)}")
        tasks[task_id] = {"status": "failed", "error": str(e)}

async def wait_for_image(task_id: str, prompt_id: str):
    try:
        image_data = await get_image(prompt_id)
        tasks[task_id] = {"status": "completed", "image": image_data}
    except Exception as e:
        logger.error(f"Error in wait_for_image: {str(e)}")
        tasks[task_id] = {"status": "failed", "error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)