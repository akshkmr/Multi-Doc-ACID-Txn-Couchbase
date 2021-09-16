package com.example.MultiDocAcidTxn.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;

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
