package com.cloud.webapp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.webapp.dao.HealthDAO;
import com.cloud.webapp.model.Health;


@Service
public class HealthService {

	@Autowired
    private HealthDAO healthDao;

    public boolean performHealthCheck() {
        try {
        	healthDao.save(new Health(LocalDateTime.now()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
