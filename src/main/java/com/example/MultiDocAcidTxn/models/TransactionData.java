package com.example.MultiDocAcidTxn.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionData {
    String  customer1Id;
    String  customer2Id;
    Integer amount;
}
