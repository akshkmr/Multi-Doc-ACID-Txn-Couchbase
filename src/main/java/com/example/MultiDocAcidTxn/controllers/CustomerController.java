package com.example.MultiDocAcidTxn.controllers;

import com.couchbase.transactions.Transactions;
import com.example.MultiDocAcidTxn.exception.CustomerNotFound;
import com.example.MultiDocAcidTxn.models.Customer;
import com.example.MultiDocAcidTxn.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    Transactions transactions;

    @Autowired
    CouchbaseClientFactory couchbaseClientFactory;

    @Autowired
    MappingCouchbaseConverter mappingCouchbaseConverter;



    @PostMapping("/customer/create")
    @ResponseStatus(HttpStatus.CREATED)
    public String createCustomer(@RequestBody Customer customer){
        customerRepository.save(customer);
        return "customer saved successfully!!!";
    }

    @GetMapping
    @RequestMapping("/customer/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerById(@PathVariable String id){
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFound());
    }

    @GetMapping
    @RequestMapping("/customer/all")
    @ResponseStatus(HttpStatus.OK)
    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }
}
