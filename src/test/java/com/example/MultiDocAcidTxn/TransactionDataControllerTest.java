package com.example.MultiDocAcidTxn;


import com.example.MultiDocAcidTxn.models.Customer;
import com.example.MultiDocAcidTxn.models.TransactionData;
import com.example.MultiDocAcidTxn.repositories.CustomerRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


import java.io.IOException;
import java.util.Arrays;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MultiDocAcidTxnApplication.class)
@WebAppConfiguration
public class TransactionDataControllerTest {

    private static final Long USER_ONE_ID = 1L;
    private static final Long USER_TWO_ID = 2L;
    private static final Long USER_THREE_ID = 3L;
    private static final Long USER_FOUR_ID = 4L;
    private static final Long USER_FIVE_ID = 5L;
    private static final Long USER_SIX_ID = 6L;
    private static final String USER_ONE_KEY = Customer.getKeyFor(USER_ONE_ID);
    private static final String USER_TWO_KEY = Customer.getKeyFor(USER_TWO_ID);
    private static final String USER_THREE_KEY = Customer.getKeyFor(USER_THREE_ID);
    private static final String USER_FOUR_KEY = Customer.getKeyFor(USER_FOUR_ID);
    private static final String USER_FIVE_KEY = Customer.getKeyFor(USER_FIVE_ID);
    private static final String USER_SIX_KEY = Customer.getKeyFor(USER_SIX_ID);
    private static MediaType CONTENT_TYPE =
            new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype());

    private MockMvc mockMvc;
    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    public void setConverters(HttpMessageConverter<?>[] converters) {
        mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);
        Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void init(){
        mockMvc = webAppContextSetup(webApplicationContext).build();

        Customer customer1 = new Customer(USER_ONE_KEY, 100, "Tushar", "Test");
        Customer customer2 = new Customer(USER_TWO_KEY, 100, "Akshay", "Test");
        Customer customer3 = new Customer(USER_THREE_KEY, 100, "Kush", "Test");
        Customer customer4 = new Customer(USER_FOUR_KEY, 100, "Aniket", "Test");
        Customer customer5 = new Customer(USER_FIVE_KEY, 100, "Raven", "Test");
        Customer customer6 = new Customer(USER_SIX_KEY, 100, "Mahesh", "Test");
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
        customerRepository.save(customer4);
        customerRepository.save(customer5);
        customerRepository.save(customer6);
    }

    @After
    public void tearDown(){
        // delete all the test data created from the database
        customerRepository.deleteById(USER_ONE_KEY);
        customerRepository.deleteById(USER_TWO_KEY);
        customerRepository.deleteById(USER_THREE_KEY);
        customerRepository.deleteById(USER_FOUR_KEY);
        customerRepository.deleteById(USER_FIVE_KEY);
        customerRepository.deleteById(USER_SIX_KEY);
    }

    @Test(expected = NestedServletException.class)
    public void getNonExistingUser() throws Exception {
        mockMvc.perform(get("/customer/100")).andExpect(status().isNotFound()).andExpect(content().contentType(CONTENT_TYPE));
    }

    @Test
    public void getExistingUser() throws Exception {
        mockMvc.perform(get("/customer/" + USER_ONE_KEY)).andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.id", Matchers.is("user::1")))
                .andExpect(jsonPath("$.name", Matchers.is("Tushar")))
                .andExpect(jsonPath("$.balance", Matchers.is(100)))
                .andExpect(jsonPath("$.type", Matchers.is("Test")));
    }

    @Test
    public void createUser() throws Exception {
        Customer customer =
                new Customer("user::create::test", 100, "Raven", "Test");
        mockMvc.perform(post("/customer/create").contentType(CONTENT_TYPE).content(json(customer))).andExpect(status().isCreated());
        customerRepository.deleteById("user::create::test");
    }

    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    protected void executeGet(Integer id) throws Exception {
        TransactionData transactionData = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + id).amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData)));
    }

    @Test()
    public void postTransactions() throws Exception{
        TransactionData transactionData1 = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + "2").amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData1)));
        TransactionData transactionData2 = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + "3").amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData2)));
        TransactionData transactionData3 = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + "4").amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData3)));
        TransactionData transactionData4 = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + "5").amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData4)));
        TransactionData transactionData5 = TransactionData.builder().customer1Id("user::1").customer2Id("user::" + "6").amount(10).build();
        mockMvc.perform(post("/transaction").contentType(CONTENT_TYPE).content(json(transactionData5)));

        mockMvc.perform(get("/customer/" + USER_ONE_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(50)));
        mockMvc.perform(get("/customer/" + USER_TWO_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(110)));
        mockMvc.perform(get("/customer/" + USER_THREE_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(110)));
        mockMvc.perform(get("/customer/" + USER_FOUR_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(110)));
        mockMvc.perform(get("/customer/" + USER_FIVE_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(110)));
        mockMvc.perform(get("/customer/" + USER_SIX_KEY)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", Matchers.is(110)));
    }

    @Test(expected = NestedServletException.class)
    public void customerNotFound() throws Exception{
        mockMvc.perform(get("/customer/" + "user::test"));
    }

}
