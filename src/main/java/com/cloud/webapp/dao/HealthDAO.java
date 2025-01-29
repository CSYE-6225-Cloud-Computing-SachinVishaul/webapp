package com.cloud.webapp.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import com.cloud.webapp.exceptions.DataAccessException;
import com.cloud.webapp.model.Health;
import com.cloud.webapp.util.HibernateUtil;


@Repository
public class HealthDAO {

private SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
	
	public void save(Health health) {
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.getTransaction();
            transaction.begin();

            session.merge(health);
            transaction.commit();
            
        }
        catch (Exception e){
            e.printStackTrace();
            throw new DataAccessException("Failed to save health data", e);
        }

    }
}
