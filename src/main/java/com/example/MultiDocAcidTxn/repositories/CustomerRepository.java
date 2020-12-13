package com.example.MultiDocAcidTxn.repositories;

import com.example.MultiDocAcidTxn.models.Customer;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CouchbaseRepository<Customer, String> {
}
