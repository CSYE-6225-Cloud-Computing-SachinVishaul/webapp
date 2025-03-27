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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.webapp.model.File;
import com.cloud.webapp.service.FileService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
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
    private static final Logger logger = LoggerFactory.getLogger(S3Controller.class);

    
    private final Counter listObjectsCounter;
    private final Timer listObjectsTimer;
    private final Counter uploadFileCounter;
    private final Timer uploadFileTimer;
    private final Counter getFileCounter;
    private final Timer getFileTimer;
    private final Counter deleteFileCounter;
    private final Timer deleteFileTimer;
    
    @Autowired
    FileService fileService;
    private boolean hasExtraQueryParams(HttpServletRequest request) {
        // For endpoints that expect no query parameters, the parameter map must be empty.
        return request.getParameterMap() != null && !request.getParameterMap().isEmpty();
    }

    public S3Controller(@Value("${aws.s3.bucket}") String bucketName,
                        @Value("${aws.region}") String region,FileService fileService,
                        MeterRegistry meterRegistry) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.fileService = fileService;
        this.listObjectsCounter = meterRegistry.counter("api.s3.list.count");
        this.listObjectsTimer = meterRegistry.timer("api.s3.list.timer");
        this.uploadFileCounter = meterRegistry.counter("api.s3.upload.count");
        this.uploadFileTimer = meterRegistry.timer("api.s3.upload.timer");
        this.getFileCounter = meterRegistry.counter("api.s3.get.count");
        this.getFileTimer = meterRegistry.timer("api.s3.get.timer");
        this.deleteFileCounter = meterRegistry.counter("api.s3.delete.count");
        this.deleteFileTimer = meterRegistry.timer("api.s3.delete.timer");
    }

    // Endpoint to list all objects in the bucket
    @GetMapping("/list")
    public ResponseEntity<List<String>> listObjects(HttpServletRequest request) {
    	return listObjectsTimer.record(() -> {
    	listObjectsCounter.increment();
    	logger.info("Listing S3 bucket objects requested");
    	if (hasExtraQueryParams(request)) {
    		logger.warn("List objects request has invalid query parameters");
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
            logger.info("Successfully retrieved {} objects from bucket", keys.size());
            return ResponseEntity.ok(keys);
        } catch (Exception e) {
        	logger.error("Error listing objects in S3 bucket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    	});
    }
    
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("profilePic") MultipartFile file, HttpServletRequest request) {
    	return uploadFileTimer.record(() -> {
    	uploadFileCounter.increment();
    	logger.info("File upload initiated: filename={}", file.getOriginalFilename());
    	try {
            // For a multipart request, we expect no query parameters.
            if (hasExtraQueryParams(request)) {
            	logger.warn("File upload request has invalid query parameters");
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
            logger.info("File uploaded successfully: id={}", savedFile.getId());
            Map<String, String> response = new HashMap<>();
            response.put("file_name", savedFile.getFileName());
            response.put("id", savedFile.getId());
            response.put("url", savedFile.getUrl());
            response.put("upload_date", savedFile.getUploadDate().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    	});
    }

    
    @GetMapping
    public ResponseEntity<Void> getMethodNotAllowed() {
    	logger.warn("Invalid GET request made to /v1/file without specifying ID or action");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable("id") String id,HttpServletRequest request) {
    	return getFileTimer.record(() -> {
    	getFileCounter.increment();
    	logger.info("File retrieval requested: id={}", id);
    	if (hasExtraQueryParams(request)) {
    		logger.warn("File retrieval request has invalid query parameters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        File fileEntity = fileService.getFileById(id);
        if (fileEntity == null) {
        	logger.warn("File not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        logger.info("File retrieved successfully: id={}", id);
        Map<String, String> response = new HashMap<>();
        response.put("file_name", fileEntity.getFileName());
        response.put("id", fileEntity.getId());
        response.put("url", fileEntity.getUrl());
        response.put("upload_date", fileEntity.getUploadDate().toString());
        return ResponseEntity.ok(response);
    	});
    }


 // For POST requests on /v1/file/{id} which are not allowed
    @RequestMapping(path = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<Void> postNotAllowedWithId(@PathVariable("id") String id, HttpServletRequest request) {
    	logger.warn("POST method not allowed for file id={}", id);
    	if (hasExtraQueryParams(request)) {
    		logger.warn("Request has invalid query parameters on POST method not allowed with id={}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // For DELETE requests on /v1/file (without an ID) which are not allowed
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotAllowed() {
    	logger.warn("Invalid DELETE request made to /v1/file without ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable("id") String id,HttpServletRequest request) {
    	return deleteFileTimer.record(() -> {
    	deleteFileCounter.increment();
    	logger.info("File deletion requested: id={}", id);
    	if (hasExtraQueryParams(request)) {
    		logger.warn("File deletion request has invalid query parameters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        boolean deleted = fileService.deleteFile(id);
        if (!deleted) {
        	logger.warn("File not found or unable to delete: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        logger.info("File deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    	});
    }

    
    @RequestMapping(method = { RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public ResponseEntity<Void> methodNotAllowed(HttpServletRequest request) {
    	logger.warn("Method not allowed: method={}, path=/v1/file", request.getMethod());
    	if (hasExtraQueryParams(request)) {
    		logger.warn("Request has invalid query parameters on method not allowed request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // Return 405 Method Not Allowed (no JSON) for HEAD, PUT, PATCH, OPTIONS on /v1/file/{id}
    @RequestMapping(path = "/{id}", method = { RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public ResponseEntity<Void> methodNotAllowedWithId(@PathVariable("id") String id,HttpServletRequest request) {
    	logger.warn("Method not allowed on file id={}: method={}", id, request.getMethod());
    	if (hasExtraQueryParams(request)) {
    		logger.warn("Request has invalid query parameters on method not allowed request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

}
