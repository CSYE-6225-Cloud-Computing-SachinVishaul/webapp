package com.cloud.webapp.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import com.cloud.webapp.model.File;
import com.cloud.webapp.util.HibernateUtil;


@Repository
public class FileDAO {

    public void save(File file) {
        Session session = HibernateUtil.buildSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(file);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public File findById(String id) {
        Session session = HibernateUtil.buildSessionFactory().openSession();
        try {
            return session.get(File.class, id);
        } finally {
            session.close();
        }
    }

    public void delete(File file) {
        Session session = HibernateUtil.buildSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(file);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
