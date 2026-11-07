package com.mentor.paymentservice.aba;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionConfig {

    /**
     * Lets a service run ONE short database step on demand, instead of wrapping a whole method in
     * @Transactional. That is what keeps remote calls (ABA, order-service) outside transactions.
     */
    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}