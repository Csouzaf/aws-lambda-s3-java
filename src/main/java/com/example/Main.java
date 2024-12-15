package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {
    
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        private final S3Client s3Client = S3Client.builder().build();
        

        @Override
        public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        
            String body = input.get("body").toString();

            Map<String, String> bodyMap;
            
            try {
                
                bodyMap = objectMapper.readValue(body, Map.class);
                
            } catch (Exception e) {
                
            throw new RuntimeException("Error parsing JSON body" + e.getMessage(), e);
            }
            
            String originalURL = bodyMap.get("originalUrl");
            String expirationTime = bodyMap.get("expirationTime");
            
            String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);
            
            long expirationTimeInSeconds = Long.parseLong(expirationTime);
            
            UrlData urlData = new UrlData(originalURL, expirationTimeInSeconds);
            
            try {
            	
            	String urlDataJson = objectMapper.writeValueAsString(originalURL);
            	
            	PutObjectRequest request = PutObjectRequest.builder()
            			.bucket("s3-java-aws-lambda")
            			.key(shortUrlCode + ".json")
            			.build();
            			
            	s3Client.putObject(request, RequestBody.fromString(urlDataJson));         	
            	
			} catch (Exception e) {
				throw new RuntimeException("Error saving Url data to S3: " + e.getMessage(), e);
			}
            
            Map<String, String> response = new HashMap<String, String>();
            
            response.put("code", shortUrlCode);
            
            return response;
        
    }
  
    
}