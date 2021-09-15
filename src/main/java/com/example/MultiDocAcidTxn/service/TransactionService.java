package com.example.MultiDocAcidTxn.service;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.error.TransactionCommitAmbiguous;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.transactions.log.LogDefer;
import com.example.MultiDocAcidTxn.controllers.TransactionController;
import com.example.MultiDocAcidTxn.exception.CustomerNotFound;
import com.example.MultiDocAcidTxn.exception.InsufficientFunds;
import com.example.MultiDocAcidTxn.models.Transaction;
import com.example.MultiDocAcidTxn.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TransactionService {
    @Autowired
    Transactions transactions;

    @Autowired
    CouchbaseClientFactory couchbaseClientFactory;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public String transact(TransactionData transactionData) {
        AtomicReference<String> transferId = new AtomicReference<>();

        try {
            transactions.run(transactionContext -> {
                // getOrError means "fail the transaction if that key does not exist"
                TransactionGetResult customer1 = transactionContext.get(couchbaseClientFactory.getDefaultCollection(), transactionData.getCustomer1Id());
                TransactionGetResult customer2 = transactionContext.get(couchbaseClientFactory.getDefaultCollection(), transactionData.getCustomer2Id());

                JsonObject customer1Content = customer1.contentAsObject();
                JsonObject customer2Content = customer2.contentAsObject();

                logger.info("In transaction - got customer 1's details: " + customer1Content);
                logger.info("In transaction - got customer 2's details: " + customer2Content);

                int customer1Balance = customer1Content.getInt("balance");
                int customer2Balance = customer2Content.getInt("balance");

                Transaction transaction = Transaction.builder()
                        .transfererName(customer1Content.get("name").toString())
                        .transfererId(transactionData.getCustomer1Id())
                        .transfereeId(transactionData.getCustomer2Id())
                        .amount(transactionData.getAmount())
                        .type("Transfer")
                        .build();

                transferId.set(UUID.randomUUID().toString());

                transactionContext.insert(couchbaseClientFactory.getDefaultCollection(), transferId.get(), transaction);

                logger.info("In transaction - creating record of transfer with UUID: " + transferId.get());

                if (customer1Balance >= transactionData.getAmount()) {
                    logger.info("In transaction - customer 1 has sufficient balance, transferring " + transactionData.getAmount());

                    customer1Content.put("balance", customer1Balance - transactionData.getAmount());
                    customer2Content.put("balance", customer2Balance + transactionData.getAmount());

                    logger.info("In transaction - changing customer 1's balance to: " + customer1Content.getInt("balance"));
                    logger.info("In transaction - changing customer 2's balance to: " + customer2Content.getInt("balance"));

                    transactionContext.replace(customer1, customer1Content);
                    transactionContext.replace(customer2, customer2Content);
                } else {
                    logger.info("In transaction - customer 1 has insufficient balance to transfer " + transactionData.getAmount());

                    // Rollback is automatic on a thrown exception.  This will also cause the transaction to fail
                    // with a TransactionFailed containing this InsufficientFunds as the getCause() - see below.
                    throw new InsufficientFunds();
                }


                // If we reach here, commit is automatic.
                logger.info("In transaction - about to commit");
                // ctx.commit(); // can also, and optionally, explicitly commit

            });
        } catch(TransactionCommitAmbiguous err) {
            System.err.println("Transaction " + err.result().transactionId() + " possibly committed:");
            for (LogDefer log : err.result().log().logs()) {
                System.err.println(log);
            }
        } catch (TransactionFailed err){
            if(err.getCause() instanceof  InsufficientFunds){
                throw (RuntimeException) err.getCause();
            }
            // ctx.getOrError can raise a DocumentNotFoundException
            else if(err.getCause() instanceof DocumentNotFoundException){
                throw new CustomerNotFound();
            } else {
                // Unexpected error - log for human review
                // This per-txn log allows the app to only log failures
                System.err.println("Transaction " + err.result().transactionId() + " did not reach commit:");

                err.result().log().logs().forEach(System.err::println);
            }
        }

        // Post-transaction, see the results:
        JsonObject customer1 = couchbaseClientFactory.getDefaultCollection().get(transactionData.getCustomer1Id()).contentAsObject();
        JsonObject customer2 = couchbaseClientFactory.getDefaultCollection().get(transactionData.getCustomer2Id()).contentAsObject();

        logger.info("After transaction - got customer 1's details: " + customer1);
        logger.info("After transaction - got customer 2's details: " + customer2);

        if (transferId.get() != null) {
            JsonObject transferRecord = couchbaseClientFactory.getDefaultCollection().get(transferId.get()).contentAsObject();

            logger.info("After transaction - transfer record: " + transferRecord);
        }

        return transferId.toString();
    }
}
