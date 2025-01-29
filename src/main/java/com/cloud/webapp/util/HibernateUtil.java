package com.cloud.webapp.util;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import com.cloud.webapp.model.*;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

	public static SessionFactory buildSessionFactory(){
        Map<String, Object> settings = new HashMap<>();
      	settings.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/healthcheckdb");
      	settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
      	settings.put("hibernate.connection.username", "root");
      	settings.put("hibernate.connection.password","root");

        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        settings.put("hibernate.dialect.storage_engine", "innodb");
        settings.put("hibernate.show-sql", "true");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addPackage("com.cloud.assignmentOne.model");
        metadataSources.addAnnotatedClasses(Health.class);

        Metadata metadata = metadataSources.buildMetadata();

        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

        return sessionFactory;
    }
	
}
