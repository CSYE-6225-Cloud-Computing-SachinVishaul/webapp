package com.cloud.webapp.service;

import com.cloud.webapp.dao.FileDAO;
import com.cloud.webapp.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.UUID;

@Service
public class FileService {

	
    private final FileDAO fileDAO;
    private final S3Client s3Client;
    private final String bucketName;

    @Autowired
    public FileService(FileDAO fileDAO,
                       @Value("${aws.s3.bucket}") String bucketName,
                       @Value("${aws.region}") String region) {
        this.fileDAO = fileDAO;
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public File uploadFile(MultipartFile file) throws Exception {
    	
    	String uuid = UUID.randomUUID().toString();
        
    	String s3Key = uuid + "/" + file.getOriginalFilename();

        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        
        String url = bucketName + "/" + s3Key;

        
        File fileEntity = new File(uuid, file.getOriginalFilename(), url);

        
        fileDAO.save(fileEntity);

        return fileEntity;
    }

    public File getFileById(String id) {
        File fileEntity = fileDAO.findById(id);
        if (fileEntity == null) {
            return null;
        }
        
        try {
            String s3Key = extractS3Key(fileEntity.getUrl());
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.headObject(headRequest);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return null;
            }
            throw e;
        }
        return fileEntity;
    }

    public boolean deleteFile(String id) {
        File fileEntity = fileDAO.findById(id);
        if (fileEntity == null) {
            return false;
        }
        
        String s3Key = extractS3Key(fileEntity.getUrl());

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
        }
        // Delete from the database
        fileDAO.delete(fileEntity);
        return true;
    }


    private String extractS3Key(String url) {
        String prefix = bucketName + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return url;
    }
}
