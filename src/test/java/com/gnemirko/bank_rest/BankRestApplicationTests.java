package com.gnemirko.bank_rest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.liquibase.enabled=false")
class BankRestApplicationTests {

    @Test
    void contextLoads() {
    }

}
