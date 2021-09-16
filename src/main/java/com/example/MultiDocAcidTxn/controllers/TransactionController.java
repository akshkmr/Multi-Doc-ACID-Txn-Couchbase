package com.example.MultiDocAcidTxn.controllers;

import com.example.MultiDocAcidTxn.exception.InsufficientFunds;
import com.example.MultiDocAcidTxn.models.Transaction;
import com.example.MultiDocAcidTxn.models.TransactionData;
import com.example.MultiDocAcidTxn.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;


    @PostMapping(path = "/transaction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String transact(@RequestBody TransactionData transactionData) {
        try {
           return transactionService.transact(transactionData);
        }catch (InsufficientFunds e) {
           return "Insufficient Balance";
        }
    }

    @GetMapping(path = "/transactions/all/customerName/{customerName}")
    @ResponseStatus(HttpStatus.OK)
    public List<Transaction> allCustomerTransactions(@PathVariable("customerName") String customerName) {
        return transactionService.findAllCustomerTransactions(customerName);
    }

    @GetMapping(path = "/transactions/recent/customerName/{customerName}")
    @ResponseStatus(HttpStatus.OK)
    public List<Transaction> recentCustomerTransactions(@PathVariable("customerName") String customerName) {
        return transactionService.findRecentCustomerTransactions(customerName);
    }
}
