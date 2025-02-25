package com.cloud.webapp;

import static io.restassured.RestAssured.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cloud.webapp.service.HealthService;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebappApplicationTests {
	
	@LocalServerPort
    private int port;
	
	@MockBean
    private HealthService healthServiceMock;
	
	@Autowired
	private HealthService healthService;
	
    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port; 
    }

    @Test
    @Order(1)
    void shouldReturn200WhenDbIsConnected() {
    	when(healthServiceMock.isDbConnected()).thenReturn(true);
        given().
            contentType(ContentType.JSON).
        when().
            get("/healthz").
        then().
            statusCode(HttpStatus.BAD_REQUEST.value());
            
//            .header("Cache-Control", equalTo("no-cache"));
    }

    @Test
    @Order(2)
    void shouldReturn400WhenBodyIsProvided() {
        given().
            contentType(ContentType.JSON).
            body("test-body").
        when().
            get("/healthz").
        then().
            statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(3)
    void shouldReturn400WhenInvalidQueryParamsAreUsed() {
        given().
            queryParam("invalidParam", "value").
        when().
            get("/healthz").
        then().
            statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(4)
    void shouldReturn405ForUnsupportedHttpMethods() {
        String[] methods = {"POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"};
        
        for (String method : methods) {
            given().
                contentType(ContentType.JSON).
            when().
                request(method, "/healthz").
            then().
                statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        }
    }
    
    @Test
    @Order(5)
    void shouldReturn503WhenDbIsNotConnected() {
        when(healthServiceMock.isDbConnected()).thenReturn(false);

        given().
            contentType(ContentType.JSON).
        when().
            get("/healthz").
        then().
            statusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
    }
}

