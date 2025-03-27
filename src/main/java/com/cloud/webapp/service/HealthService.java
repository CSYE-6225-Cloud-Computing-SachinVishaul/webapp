package com.cloud.webapp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.webapp.dao.HealthDAO;
import com.cloud.webapp.model.Health;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Service
public class HealthService {

	@Autowired
    private HealthDAO healthDao;
    private final Timer healthServiceTimer;
    private final Counter healthServiceCounter;
	
	@Autowired
    public HealthService(HealthDAO healthDao, MeterRegistry meterRegistry) {
        this.healthDao = healthDao;
        this.healthServiceTimer = meterRegistry.timer("health.service.timer");
        this.healthServiceCounter = meterRegistry.counter("health.service.count");
    }

    public boolean isDbConnected() {
    	return healthServiceTimer.record(() -> {
    	healthServiceCounter.increment();
        try {
        	healthDao.save(new Health(LocalDateTime.now()));
            return true;
        } catch (Exception e) {
            return false;
        }
    	});
    }
}
