package com.example.MultiDocAcidTxn.controllers;

import com.example.MultiDocAcidTxn.models.Customer;
import com.example.MultiDocAcidTxn.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @PostMapping("/customer/create")
    @ResponseStatus(HttpStatus.CREATED)
    public String createCustomer(@RequestBody Customer customer){
        customerService.createCustomer(customer);
        return "customer saved successfully!!!";
    }

    @GetMapping
    @RequestMapping("/customer/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerById(@PathVariable String id){
        return customerService.getCustomerById(id);
    }
}
