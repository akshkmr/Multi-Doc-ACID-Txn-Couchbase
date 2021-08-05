package com.example.MultiDocAcidTxn.service;

import com.example.MultiDocAcidTxn.exception.CustomerNotFound;
import com.example.MultiDocAcidTxn.models.Customer;
import com.example.MultiDocAcidTxn.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public void createCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public Customer getCustomerById(String id) {
        return customerRepository.findById(id).orElseThrow(CustomerNotFound::new);
    }
}
