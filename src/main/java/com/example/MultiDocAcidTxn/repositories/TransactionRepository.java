package com.example.MultiDocAcidTxn.repositories;

import com.example.MultiDocAcidTxn.models.Transaction;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CouchbaseRepository<Transaction, String> {
    @Query("#{#n1ql.selectEntity} WHERE #{#n1ql.filter} AND `type`='Transfer'")
    List<Transaction> findTransactionsByCustomerName(String name);
}