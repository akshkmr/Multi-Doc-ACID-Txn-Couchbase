package com.example.MultiDocAcidTxn.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;

@Data
@Builder
@Document
public class Transaction {
    private String id;
    private String transfererName;
    private String transfererId;
    private String transfereeId;
    private Integer amount;
    private String type;
}
