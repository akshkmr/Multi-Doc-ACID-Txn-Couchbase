# Multi Document ACID Transactions with Spring Data Couchbase

A spring based RESTful API for money transfers between users accounts

### Technologies
- Spring Boot
- Spring Data Couchbase
- Couchbase Client 
- Couchbase Server

### Requirements
- Couchbase Server 6.5 or above
- Couchbase Java client 3.0.0 or above. It is recommended to follow the transitive dependency for the transactions library from maven.

### How to run
```sh
mvn exec:java
```

Application starts a tomcat server on localhost port 8080. Couchbae server initialized with some sample customer account data To view


### Available Services

| HTTP METHOD | PATH | USAGE |
| -----------| ------ | ------ |
| GET | /customer/{id} | get customer by id | 
| GET | /customer/all | get all customers | 
| POST | /customer/create | create a new customer | 
| POST | /transaction | perform transaction between 2 user accounts | 

### Http Status
- 200 OK: The request has succeeded
- 400 Bad Request: The request could not be understood by the server 
- 404 Not Found: The requested resource cannot be found
- 500 Internal Server Error: The server encountered an unexpected condition 

### Sample JSON for Customer and Account

##### Customer Account: : 

```sh
{
   "id": "03a587dc-6caa-4991-8e65-c1503be0316e",
   "balance": 90,
   "name": "Vishal",
   "type": "Customer"
}
```

#### User Transaction:
```sh
{
	"customer1Id":"03a587dc-6caa-4991-8e65-c1503be0316e",
	"customer2Id":"9707685a-f5ef-4cda-be26-c449556166ad",
	"amount":20
}
```
### Transaction Access
```sh
@Autowired
Transactions transactions;

@Autowired
CouchbaseClientFactory couchbaseClientFactory;

public void doSomething() {
 transactions.run(ctx -> {
  ctx.insert(couchbaseClientFactory.getDefaultCollection(), "id", "content");
  ctx.commit();
 });
}
```

### Transaction Configuration
```sh
@Configuration
static class Config extends AbstractCouchbaseConfiguration {

    // Usual Setup
    @Override public String getConnectionString() { /* ... */ }
    @Override public String getUserName() { /* ... */ }
    @Override public String getPassword() { /* ... */ }
    @Override public String getBucketName() { /* ... */ }

	@Bean
	public Transactions transactions(final Cluster couchbaseCluster) {
		return Transactions.create(couchbaseCluster, TransactionConfigBuilder.create()
			// The configuration can be altered here, but in most cases the defaults are fine.
			.build());
	}

}
```

### Reference Documentation
https://docs.spring.io/spring-data/couchbase/docs/current/reference/html/#couchbase.transactions

