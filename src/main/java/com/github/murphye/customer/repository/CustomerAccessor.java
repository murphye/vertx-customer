package com.github.murphye.customer.repository;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.github.murphye.customer.Customer;
import com.google.common.util.concurrent.ListenableFuture;

@Accessor
public interface CustomerAccessor {

    @Query("SELECT * FROM customer")
    ListenableFuture<Result<Customer>> findAll();

}
