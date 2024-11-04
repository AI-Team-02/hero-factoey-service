// ImageController.java
package ai.herofactoryservice.gameobject.controller;

import ai.herofactoryservice.gameobject.service.GameObjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class GameObjectController {
    
    private final GameObjectService gameObjectService;


    @Operation(
            summary = "게임 오브젝트 이미지 생성",
            description = "이미지와 prompt로 게임 오브젝트 이미지 생성"
    )
    @PostMapping(value = "/process-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> processImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("prompt") String prompt
    ) {
        try {
            byte[] processedImage = gameObjectService.sendImageToFastAPI(file, prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("processed_image.png").build());
            
            return new ResponseEntity<>(processedImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing image: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "로그인 인증 테스트 API",
            description = "로그인 토큰으로 접근 가능한지 테스트하는 엔드포인트입니다."
    )
    @GetMapping
    public ResponseEntity<String> test(){
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
}