package com.example.MultiDocAcidTxn.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Transaction {
    private String transfererName;
    private String transfererId;
    private String transfereeId;
    private Integer amount;
    private String type;
}
