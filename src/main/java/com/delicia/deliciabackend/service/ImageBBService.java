package com.delicia.deliciabackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;

@Service
public class ImageBBService {

    @Value("${imgbb.api.key}")
    private String apiKey;

    private final String UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public String uploadImage(MultipartFile file) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", apiKey);
        body.add("image", base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(UPLOAD_URL, requestEntity, String.class);

        // Extraer el enlace de la imagen del JSON de respuesta
        if (response.getStatusCode() == HttpStatus.OK) {
            String bodyStr = response.getBody();
            int start = bodyStr.indexOf("\"url\":\"") + 7;
            int end = bodyStr.indexOf("\"", start);
            String imageUrl = bodyStr.substring(start, end);
            return imageUrl;
        } else {
            throw new Exception("Error al subir imagen a ImageBB: " + response.getBody());
        }
    }
}