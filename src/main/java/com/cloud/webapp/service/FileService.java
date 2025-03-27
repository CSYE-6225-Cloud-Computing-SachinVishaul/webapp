package com.cloud.webapp.service;

import com.cloud.webapp.dao.FileDAO;
import com.cloud.webapp.model.File;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

	
    private final FileDAO fileDAO;
    private final S3Client s3Client;
    private final String bucketName;
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    // Metrics for S3 operations
    private final Counter s3UploadCounter;
    private final Timer s3UploadTimer;
    private final Counter s3HeadCounter;
    private final Timer s3HeadTimer;
    private final Counter s3DeleteCounter;
    private final Timer s3DeleteTimer;

    
    @Autowired
    public FileService(FileDAO fileDAO,
                       @Value("${aws.s3.bucket}") String bucketName,
                       @Value("${aws.region}") String region, MeterRegistry meterRegistry) {
        this.fileDAO = fileDAO;
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.s3UploadCounter = meterRegistry.counter("s3.operation.upload.count");
        this.s3UploadTimer = meterRegistry.timer("s3.operation.upload.timer");
        this.s3HeadCounter = meterRegistry.counter("s3.operation.head.count");
        this.s3HeadTimer = meterRegistry.timer("s3.operation.head.timer");
        this.s3DeleteCounter = meterRegistry.counter("s3.operation.delete.count");
        this.s3DeleteTimer = meterRegistry.timer("s3.operation.delete.timer");
    }

    public File uploadFile(MultipartFile file) throws Exception {
    	
    	return s3UploadTimer.recordCallable(() -> {
    	s3UploadCounter.increment();
    	String uuid = UUID.randomUUID().toString();
        
    	String s3Key = uuid + "/" + file.getOriginalFilename();
    	
    	logger.info("Uploading file to S3: filename={}, s3Key={}", file.getOriginalFilename(), s3Key);

        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        try {
			s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
		} catch (Exception e) {
			logger.error("Error during S3 putObject: filename={}, s3Key={}, error={}", 
                    file.getOriginalFilename(), s3Key, e.getMessage());
			throw e;
		}

        
        String url = bucketName + "/" + s3Key;

        
        File fileEntity = new File(uuid, file.getOriginalFilename(), url);

        
        fileDAO.save(fileEntity);
        
        logger.info("File saved successfully to DB and S3: id={}", uuid);

        return fileEntity;
    	});
    }

    public File getFileById(String id) {
    	
    	return s3HeadTimer.record(() -> {
    	
    	s3HeadCounter.increment();
    	logger.info("Retrieving file by ID: {}", id);
        File fileEntity = fileDAO.findById(id);
        if (fileEntity == null) {
        	logger.warn("File not found in DB: id={}", id);
            return null;
        }
        
        try {
            String s3Key = extractS3Key(fileEntity.getUrl());
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.headObject(headRequest);
            logger.info("File found in S3: s3Key={}", s3Key);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
            	logger.warn("File missing in S3: id={}, s3Key={}", id, fileEntity.getUrl());
                return null;
            }
            logger.error("Error retrieving file from S3: id={}", id, e);
            throw e;
        }
        return fileEntity;
    	});
    }

    public boolean deleteFile(String id) {
    	return s3DeleteTimer.record(() -> {
    	s3DeleteCounter.increment();
    	logger.info("Deleting file: id={}", id);
        File fileEntity = fileDAO.findById(id);
        if (fileEntity == null) {
        	logger.warn("Attempted to delete non-existent file: id={}", id);
            return false;
        }
        
        String s3Key = extractS3Key(fileEntity.getUrl());

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            logger.info("File deleted from S3: s3Key={}", s3Key);
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
            	logger.error("Error deleting file from S3: id={}, s3Key={}", id, s3Key, e);
                throw e;
            }
            logger.warn("File not found in S3 while deleting: id={}, s3Key={}", id, s3Key);
        }
        // Delete from the database
        fileDAO.delete(fileEntity);
        logger.info("File deleted from database: id={}", id);
        return true;
    	});
    }


    private String extractS3Key(String url) {
        String prefix = bucketName + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return url;
    }
}
