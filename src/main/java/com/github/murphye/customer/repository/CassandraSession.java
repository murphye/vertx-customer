package com.github.murphye.customer.repository;

import com.datastax.driver.core.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CassandraSession {

    private static Session session;

    @Inject
    public CassandraSession() {
        System.out.println("CassandraSession()");

        if(session == null || session.isClosed()) {
            System.out.println("Resetting C* session...");
            Cluster cluster = null;
            try {
                cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .build();
                session = cluster.connect();

                session.execute("CREATE KEYSPACE IF NOT EXISTS customer " +
                    "WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}; "
                );

                session.execute("USE customer");

                session.execute("CREATE TABLE IF NOT EXISTS customer ( " +
                    "id UUID, name TEXT, city TEXT, state TEXT, zipcode TEXT, PRIMARY KEY (id))"
                );
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Session getSession() {
        return session;
    }
}
