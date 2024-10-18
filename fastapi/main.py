import os
import json
import asyncio
import aiohttp
import logging
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, JSONResponse
from pydantic import BaseModel, validator
from typing import Optional

# 로깅 설정
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

app = FastAPI()

# ComfyUI 서버 URL
COMFY_URL = "http://127.0.0.1:8188"

# 워크플로우 파일 경로
MASK_WORKFLOW_PATH = r"C:\Users\201-6\dev\comfy\fastapi\workflow\mask_workflow_api.json"
INPAINT_WORKFLOW_PATH = r"C:\Users\201-6\dev\comfy\fastapi\workflow\inpaint_workflow_api.json"

# 입력 이미지 경로
INPUT_IMAGE_DIR = r"C:\Users\201-6\dev\comfy\fastapi\images"

# 결과 이미지 저장 경로
OUTPUT_IMAGE_DIR = r"C:\Users\201-6\dev\comfy\fastapi\output"

class WorkflowRequest(BaseModel):
    input_image: str
    mask_image: str

    @validator('input_image', 'mask_image')
    def validate_image_file(cls, v):
        if not v.endswith(('.png', '.jpg', '.jpeg')):
            raise ValueError(f'Image file must be png, jpg, or jpeg. Received: {v}')
        return v

async def load_workflow(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)

async def upload_image(session, image_path):
    if not os.path.exists(image_path):
        raise HTTPException(status_code=404, detail=f"Image not found: {image_path}")
    
    with open(image_path, 'rb') as f:
        response = await session.post(f"{COMFY_URL}/upload/image", data={'image': f})
    if response.status == 200:
        return await response.json()
    else:
        raise HTTPException(status_code=500, detail="Failed to upload image")

async def queue_prompt(session, workflow):
    response = await session.post(f"{COMFY_URL}/prompt", json={"prompt": workflow})
    if response.status == 200:
        return await response.json()
    else:
        raise HTTPException(status_code=500, detail="Failed to queue prompt")

async def get_image(session, filename):
    response = await session.get(f"{COMFY_URL}/view", params={"filename": filename})
    if response.status == 200:
        return await response.read()
    else:
        raise HTTPException(status_code=500, detail="Failed to get image")

@app.post("/run_workflow")
async def run_workflow(request: Request):
    logger.debug("Received request to /run_workflow")
    
# 요청 헤더 로깅
    headers = dict(request.headers)
    logger.debug(f"Request headers: {headers}")
    
    # 요청 본문 로깅
    body = await request.body()
    logger.debug(f"Raw request body: {body}")
    
    try:
        body_json = await request.json()
        logger.debug(f"Parsed JSON body: {body_json}")
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse JSON: {str(e)}")
        return JSONResponse(
            status_code=400, 
            content={
                "detail": "Invalid JSON in request body",
                "error": str(e),
                "received_body": body.decode('utf-8', errors='replace')
            }
        )
    try:
        input_image = body.get('input_image', '')
        mask_image = body.get('mask_image', '')
        logger.debug(f"Extracted values - input_image: {input_image}, mask_image: {mask_image}")

        request_data = WorkflowRequest(input_image=input_image, mask_image=mask_image)
    except ValueError as e:
        logger.error(f"Validation error: {str(e)}")
        return JSONResponse(status_code=400, content={"detail": str(e)})
    
    input_image_path = os.path.join(INPUT_IMAGE_DIR, request_data.input_image)
    mask_image_path = os.path.join(INPUT_IMAGE_DIR, request_data.mask_image)

    logger.debug(f"Checking for input image at: {input_image_path}")
    if not os.path.exists(input_image_path):
        logger.error(f"Input image not found: {input_image_path}")
        return JSONResponse(status_code=404, content={"detail": f"Input image not found: {request_data.input_image}"})

    logger.debug(f"Checking for mask image at: {mask_image_path}")
    if not os.path.exists(mask_image_path):
        logger.error(f"Mask image not found: {mask_image_path}")
        return JSONResponse(status_code=404, content={"detail": f"Mask image not found: {request_data.mask_image}"})

    async with aiohttp.ClientSession() as session:
        try:
            # 마스크 워크플로우 실행
            mask_workflow = await load_workflow(MASK_WORKFLOW_PATH)
            
            # 입력 이미지 업로드
            upload_result = await upload_image(session, input_image_path)
            mask_workflow["140"]["inputs"]["image"] = upload_result["name"]
            
            # 마스크 워크플로우 실행
            mask_result = await queue_prompt(session, mask_workflow)
            mask_image_name = mask_result["output"]["179"]["images"][0]["filename"]
            
            # 인페인트 워크플로우 실행
            inpaint_workflow = await load_workflow(INPAINT_WORKFLOW_PATH)
            inpaint_workflow["99"]["inputs"]["image"] = upload_result["name"]
            inpaint_workflow["134"]["inputs"]["image"] = upload_result["name"]
            inpaint_workflow["88"]["inputs"]["image"] = mask_image_name
            
            inpaint_result = await queue_prompt(session, inpaint_workflow)
            result_image_name = inpaint_result["output"]["32"]["images"][0]["filename"]
            
            # 결과 이미지 가져오기
            result_image_data = await get_image(session, result_image_name)
            
            # 결과 이미지 저장
            output_path = os.path.join(OUTPUT_IMAGE_DIR, f"result_{request_data.input_image}")
            with open(output_path, 'wb') as f:
                f.write(result_image_data)
            
            logger.debug(f"Workflow completed successfully. Result saved at: {output_path}")
            return FileResponse(output_path)
        except Exception as e:
            logger.error(f"An error occurred: {str(e)}", exc_info=True)
            return JSONResponse(status_code=500, content={"detail": f"An error occurred: {str(e)}"})

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)