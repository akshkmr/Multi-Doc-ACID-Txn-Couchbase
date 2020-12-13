package com.example.MultiDocAcidTxn.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;


@Document
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;
    private Integer balance;
    private String name;
    private String type;
}
