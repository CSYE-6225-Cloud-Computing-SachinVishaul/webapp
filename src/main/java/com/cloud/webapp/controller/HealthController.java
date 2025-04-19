package com.cloud.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.webapp.service.HealthService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping({"/healthz","/cicd"})
class HealthController {
	private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    @Autowired
    private HealthService healthService;
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;
    private final Counter methodNotAllowedCounter;
    private final Timer methodNotAllowedTimer;
    
    // Inject MeterRegistry to create and register metrics
    public HealthController(HealthService healthService, MeterRegistry meterRegistry) {
        this.healthService = healthService;
        this.healthCheckCounter = meterRegistry.counter("api.healthz.count");
        this.healthCheckTimer = meterRegistry.timer("api.healthz.timer");
        this.methodNotAllowedCounter = meterRegistry.counter("api.healthz.methodNotAllowed.count");
        this.methodNotAllowedTimer = meterRegistry.timer("api.healthz.methodNotAllowed.timer");

    }

//controller functions
    @GetMapping
    public ResponseEntity<Void> healthCheck(@RequestBody(required = false) String body, HttpServletRequest request) {
    	
    	return healthCheckTimer.record(() -> {
    	healthCheckCounter.increment();
    	logger.info("Health check request received: method={}, remoteAddress={}", request.getMethod(), request.getRemoteAddr());
    	
    	if (!request.getParameterMap().isEmpty()) {
    		logger.warn("Health check failed: unexpected query parameters detected");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body != null && !body.isEmpty()) {
        	logger.warn("Health check failed: unexpected request body detected");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (healthService.isDbConnected()) {
        	logger.info("Health check succeeded: Database connection is healthy");
        	return ResponseEntity.status(HttpStatus.OK)
                    .header("Cache-Control", "no-cache")
                    .build();
        } else {
        	logger.error("Health check failed: Database connection is unavailable");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    });
    }
    
    
    
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.TRACE})
    public ResponseEntity<Void> methodNotAllowed(@RequestBody(required = false) String body, HttpServletRequest request) {
    	
    	return methodNotAllowedTimer.record(() -> {
    	methodNotAllowedCounter.increment();
    	logger.warn("Method not allowed: method={}, remoteAddress={}", request.getMethod(), request.getRemoteAddr());
    	if (!request.getParameterMap().isEmpty()) {
    		logger.warn("Invalid request: unexpected query parameters detected");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body != null && !body.isEmpty()) {
        	logger.warn("Invalid request: unexpected request body detected");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
//        System.out.println("Control is in controller");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    	 });
    }
}


