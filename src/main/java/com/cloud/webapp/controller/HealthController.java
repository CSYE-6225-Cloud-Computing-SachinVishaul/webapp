package com.cloud.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.webapp.service.HealthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/healthz")
class HealthController {
    @Autowired
    private HealthService healthService;


    @GetMapping
    public ResponseEntity<Void> healthCheck(@RequestBody(required = false) String body, HttpServletRequest request) {

    	if (!request.getParameterMap().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (healthService.isDbConnected()) {
        	return ResponseEntity.status(HttpStatus.OK)
                    .header("Cache-Control", "no-cache")
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    
    
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.TRACE})
    public ResponseEntity<Void> methodNotAllowed(@RequestBody(required = false) String body, HttpServletRequest request) {
    	if (!request.getParameterMap().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        System.out.println("Control is in controller");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}


