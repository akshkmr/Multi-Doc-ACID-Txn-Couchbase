package com.example.MultiDocAcidTxn.controllers;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.error.TransactionCommitAmbiguous;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.transactions.log.LogDefer;
import com.example.MultiDocAcidTxn.exception.CustomerNotFound;
import com.example.MultiDocAcidTxn.exception.InsufficientFunds;
import com.example.MultiDocAcidTxn.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    Transactions transactions;

    @Autowired
    CouchbaseClientFactory couchbaseClientFactory;

    @Autowired
    MappingCouchbaseConverter mappingCouchbaseConverter;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);


    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String transact(@RequestBody Transaction transaction){

        AtomicReference<String> transferId = new AtomicReference<>();

        try {
            transactions.run(ctx -> {
                // getOrError means "fail the transaction if that key does not exist"
                TransactionGetResult customer1 = ctx.get(couchbaseClientFactory.getDefaultCollection(), transaction.getCustomer1Id());
                TransactionGetResult customer2 = ctx.get(couchbaseClientFactory.getDefaultCollection(), transaction.getCustomer2Id());

                JsonObject customer1Content = customer1.contentAsObject();
                JsonObject customer2Content = customer2.contentAsObject();

                logger.info("In transaction - got customer 1's details: " + customer1Content);
                logger.info("In transaction - got customer 2's details: " + customer2Content);

                int customer1Balance = customer1Content.getInt("balance");
                int customer2Balance = customer2Content.getInt("balance");

                // Create a record of the transfer
                JsonObject transferRecord = JsonObject.create()
                        .put("from", transaction.getCustomer1Id())
                        .put("to", transaction.getCustomer2Id())
                        .put("amount", transaction.getAmount())
                        .put("type", "Transfer");

                transferId.set(UUID.randomUUID().toString());

                ctx.insert(couchbaseClientFactory.getDefaultCollection(), transferId.get(), transferRecord);

                logger.info("In transaction - creating record of transfer with UUID: " + transferId.get());

                if (customer1Balance >= transaction.getAmount()) {
                    logger.info("In transaction - customer 1 has sufficient balance, transferring " + transaction.getAmount());

                    customer1Content.put("balance", customer1Balance - transaction.getAmount());
                    customer2Content.put("balance", customer2Balance + transaction.getAmount());

                    logger.info("In transaction - changing customer 1's balance to: " + customer1Content.getInt("balance"));
                    logger.info("In transaction - changing customer 2's balance to: " + customer2Content.getInt("balance"));

                    ctx.replace(customer1, customer1Content);
                    ctx.replace(customer2, customer2Content);
                } else {
                    logger.info("In transaction - customer 1 has insufficient balance to transfer " + transaction.getAmount());

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
        JsonObject customer1 = couchbaseClientFactory.getDefaultCollection().get(transaction.getCustomer1Id()).contentAsObject();
        JsonObject customer2 = couchbaseClientFactory.getDefaultCollection().get(transaction.getCustomer2Id()).contentAsObject();

        logger.info("After transaction - got customer 1's details: " + customer1);
        logger.info("After transaction - got customer 2's details: " + customer2);

        if (transferId.get() != null) {
            JsonObject transferRecord = couchbaseClientFactory.getDefaultCollection().get(transferId.get()).contentAsObject();

            logger.info("After transaction - transfer record: " + transferRecord);
        }

        return transferId.toString();
    }
}
