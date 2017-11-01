package com.github.murphye.customer.repository;

import com.github.murphye.customer.Customer;
import io.reactivex.Flowable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class CustomerRepository extends AbstractRepository<UUID, Customer, CustomerAccessor> {

    @Inject
    public CustomerRepository(CassandraSession cassandraSession) {
        init(cassandraSession.getSession());
    }

    public Flowable<Customer> findAll() {
        return toFlowable(accessor.findAll());
    }
}
