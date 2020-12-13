package com.example.MultiDocAcidTxn.config;

import com.couchbase.client.java.Cluster;
import com.couchbase.transactions.TransactionDurabilityLevel;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.example.MultiDocAcidTxn.models.Customer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.SimpleCouchbaseClientFactory;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions;
import org.springframework.data.couchbase.repository.config.RepositoryOperationsMapping;

import java.util.Collections;

@Configuration
@EnableAutoConfiguration
public class CouchbaseConfiguration extends AbstractCouchbaseConfiguration {
    @Autowired
    private ApplicationContext context;

    @Value("${app.couchbase.connection-string}")
    private String connectionString;

    @Value("${app.couchbase.user-name}")
    private String userName;

    @Value("${app.couchbase.password}")
    private String password;

    @Value("${app.couchbase.bucket-primary}")
    private String customerBucket;

    @Value("${app.couchbase.bucket-user}")
    private String userBucket;

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return customerBucket;
    }

    @Override
    public void configureRepositoryOperationsMapping(RepositoryOperationsMapping mapping) {
        mapping.mapEntity(Customer.class, getCouchbaseTemplate(customerBucket));
    }

    @SneakyThrows
    private CouchbaseTemplate getCouchbaseTemplate(String bucketName) {
        CouchbaseTemplate template = new CouchbaseTemplate(couchbaseClientFactory(bucketName),
                mappingCouchbaseConverter(couchbaseMappingContext(customConversions()),
                        new CouchbaseCustomConversions(Collections.emptyList())));

        template.setApplicationContext(context);
        return template;
    }

    private CouchbaseClientFactory couchbaseClientFactory(String bucketName) {
        return new SimpleCouchbaseClientFactory(couchbaseCluster(couchbaseClusterEnvironment()),
                bucketName, this.getScopeName());
    }

    @Bean
    public Transactions transactions(final Cluster couchbaseCluster) {
        return Transactions.create(couchbaseCluster, TransactionConfigBuilder.create()
                .durabilityLevel(TransactionDurabilityLevel.NONE)
                .build());
    }
}
