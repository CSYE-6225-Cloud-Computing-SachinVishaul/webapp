package com.cloud.webapp.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cloud.webapp.model.File;
import com.cloud.webapp.util.HibernateUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Repository
public class FileDAO {
	
	private final MeterRegistry meterRegistry;
	
	@Autowired
    public FileDAO(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
	
    public void save(File file) {
    	Timer.Sample sample = Timer.start(meterRegistry);
        Session session = HibernateUtil.buildSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(file);
            tx.commit();
            sample.stop(meterRegistry.timer("db.query.timer", "operation", "saveFile"));
        } catch (Exception e) {
        	sample.stop(meterRegistry.timer("db.query.timer", "operation", "saveFile"));
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public File findById(String id) {
    	Timer.Sample sample = Timer.start(meterRegistry);
        Session session = HibernateUtil.buildSessionFactory().openSession();
        try {
        	sample.stop(meterRegistry.timer("db.query.timer", "operation", "findFile"));
            return session.get(File.class, id);
        } finally {
            session.close();
        }
    }

    public void delete(File file) {
    	Timer.Sample sample = Timer.start(meterRegistry);
        Session session = HibernateUtil.buildSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(file);
            tx.commit();
            sample.stop(meterRegistry.timer("db.query.timer", "operation", "deleteFile"));
        } catch (Exception e) {
        	sample.stop(meterRegistry.timer("db.query.timer", "operation", "deleteFile"));
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
