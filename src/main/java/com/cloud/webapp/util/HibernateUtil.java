package com.cloud.webapp.util;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import com.cloud.webapp.model.*;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

	public static SessionFactory buildSessionFactory(){
        Map<String, Object> settings = new HashMap<>();
        
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        
        
        String dbUrl, dbUsername, dbPassword;
        if ("prod".equalsIgnoreCase(activeProfile)) {
            String rdsEndpoint = dotenv.get("RDS_DB_ENDPOINT");
            if (rdsEndpoint == null || rdsEndpoint.isEmpty()) {
                throw new RuntimeException("RDS_DB_ENDPOINT is not set for production.");
            }
            dbUrl = "jdbc:mysql://" + rdsEndpoint + ":3306/csye6225";
            
            dbUsername = "csye6225"; 
            
            dbPassword = dotenv.get("RDS_DB_PASSWORD");
            if (dbPassword == null || dbPassword.isEmpty()) {
                throw new RuntimeException("RDS_DB_PASSWORD is not set for production.");
            }
        } else {
            
            dbUrl = dotenv.get("DB_URL", "jdbc:mysql://localhost:3306/csye6225");
            dbUsername = dotenv.get("DB_USERNAME", "root");
            dbPassword = dotenv.get("DB_PASSWORD", "root");
        }
        
        
      	settings.put("hibernate.connection.url", dbUrl);
      	settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
      	settings.put("hibernate.connection.username", dbUsername);
      	settings.put("hibernate.connection.password",dbPassword);

        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        settings.put("hibernate.dialect.storage_engine", "innodb");
        settings.put("hibernate.show-sql", "true");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addPackage("com.cloud.assignmentOne.model");
        metadataSources.addAnnotatedClasses(Health.class);
        metadataSources.addAnnotatedClasses(File.class);
        Metadata metadata = metadataSources.buildMetadata();

        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

        return sessionFactory;
    }
	
}
