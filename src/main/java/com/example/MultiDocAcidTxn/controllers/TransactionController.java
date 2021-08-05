package com.example.MultiDocAcidTxn.controllers;

import com.example.MultiDocAcidTxn.exception.InsufficientFunds;
import com.example.MultiDocAcidTxn.models.Transaction;
import com.example.MultiDocAcidTxn.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    TransactionService transactionService;


    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String transact(@RequestBody Transaction transaction) {
        try {
           return transactionService.transact(transaction);
        }catch (InsufficientFunds e) {
           return "Insufficient Balance";
        }
    }
}
