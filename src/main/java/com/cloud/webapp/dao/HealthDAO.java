package com.cloud.webapp.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cloud.webapp.exceptions.DataAccessException;
import com.cloud.webapp.model.Health;
import com.cloud.webapp.util.HibernateUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Repository
public class HealthDAO {

private SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
private final MeterRegistry meterRegistry;

	@Autowired
	public HealthDAO(MeterRegistry meterRegistry) {
	    this.meterRegistry = meterRegistry;
	}
	
	public void save(Health health) {
		Timer.Sample sample = Timer.start(meterRegistry);
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.getTransaction();
            transaction.begin();

            session.merge(health);
            transaction.commit();
            sample.stop(meterRegistry.timer("db.query.timer", "operation", "saveHealth"));
            
        }
        catch (Exception e){
        	sample.stop(meterRegistry.timer("db.query.timer", "operation", "saveHealth"));
            e.printStackTrace();
            throw new DataAccessException("Failed to save health data", e);
        }

    }
}
