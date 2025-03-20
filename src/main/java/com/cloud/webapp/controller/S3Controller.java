package com.cloud.webapp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.webapp.model.File;
import com.cloud.webapp.service.FileService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@RestController
@RequestMapping("/v1/file")
public class S3Controller {

    private final S3Client s3Client;
    private final String bucketName;
    
    @Autowired
    FileService fileService;
    private boolean hasExtraQueryParams(HttpServletRequest request) {
        // For endpoints that expect no query parameters, the parameter map must be empty.
        return request.getParameterMap() != null && !request.getParameterMap().isEmpty();
    }

    public S3Controller(@Value("${aws.s3.bucket}") String bucketName,
                        @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    // Endpoint to list all objects in the bucket
    @GetMapping("/list")
    public ResponseEntity<List<String>> listObjects(HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            ListObjectsRequest listReq = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsResponse listRes = s3Client.listObjects(listReq);
            List<String> keys = listRes.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(keys);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("profilePic") MultipartFile file, HttpServletRequest request) {
    	try {
            // For a multipart request, we expect no query parameters.
            if (hasExtraQueryParams(request)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            // Optionally, check that only one part (the "profilePic") is present.
            if (request.getParts() != null && request.getParts().size() != 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    	try {
            File savedFile = fileService.uploadFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("file_name", savedFile.getFileName());
            response.put("id", savedFile.getId());
            response.put("url", savedFile.getUrl());
            response.put("upload_date", savedFile.getUploadDate().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    
    @GetMapping
    public ResponseEntity<Void> getMethodNotAllowed() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable("id") String id,HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        File fileEntity = fileService.getFileById(id);
        if (fileEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Map<String, String> response = new HashMap<>();
        response.put("file_name", fileEntity.getFileName());
        response.put("id", fileEntity.getId());
        response.put("url", fileEntity.getUrl());
        response.put("upload_date", fileEntity.getUploadDate().toString());
        return ResponseEntity.ok(response);
    }


 // For POST requests on /v1/file/{id} which are not allowed
    @RequestMapping(path = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<Void> postNotAllowedWithId(@PathVariable("id") String id, HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // For DELETE requests on /v1/file (without an ID) which are not allowed
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotAllowed() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable("id") String id,HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        boolean deleted = fileService.deleteFile(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    
    @RequestMapping(method = { RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public ResponseEntity<Void> methodNotAllowed(HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // Return 405 Method Not Allowed (no JSON) for HEAD, PUT, PATCH, OPTIONS on /v1/file/{id}
    @RequestMapping(path = "/{id}", method = { RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public ResponseEntity<Void> methodNotAllowedWithId(@PathVariable("id") String id,HttpServletRequest request) {
    	if (hasExtraQueryParams(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

}
