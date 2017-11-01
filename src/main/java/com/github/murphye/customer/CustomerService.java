package com.github.murphye.customer;

import com.github.murphye.customer.repository.CustomerRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@ApplicationScoped
@Path("/customer")
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @GET
    public Single<Customer> getCustomer(@PathParam("id") UUID id)
    {
        return customerRepository.find(id);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public void setCustomer(Customer customer)
    {
        Single<Void> result = customerRepository.add(customer);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Flowable<Customer> getCustomersUpperCaseName()
    {
        return customerRepository.findAll().map(
            customer -> {
                customer.setName(customer.getName().toUpperCase());
                //System.out.println("Uppercase " + customer);
                return customer;
            });
    }
}
