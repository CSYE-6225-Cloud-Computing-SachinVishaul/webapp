package com.cloud.webapp.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class File {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "file_name")
    private String fileName;  

    @Column(name = "url")
    private String url;       

    @Column(name = "upload_date")
    private LocalDate uploadDate; 


    public File() {
        // default constructor
    }

    public File(String id,String fileName, String url) {
        this.id = id;
        this.fileName = fileName;
        this.url = url;
        this.uploadDate = LocalDate.now();
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LocalDate getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(LocalDate uploadDate) {
		this.uploadDate = uploadDate;
	}

	
}
