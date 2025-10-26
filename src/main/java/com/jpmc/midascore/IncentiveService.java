package com.jpmc.midascore;

import com.jpmc.midascore.entity.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {

    private final RestTemplate restTemplate;
    private final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    public IncentiveService() {
        this.restTemplate = new RestTemplate();
    }

    public float getIncentive(Transaction transaction) {
        try {
            // Post the transaction to the API and get the incentive response
            Incentive incentive = restTemplate.postForObject(
                    INCENTIVE_API_URL,
                    transaction,
                    Incentive.class
            );

            if (incentive != null) {
                System.out.println("  Incentive received: " + incentive.getAmount());
                return incentive.getAmount();
            }

            System.out.println("  No incentive received");
            return 0.0f;

        } catch (Exception e) {
            System.err.println("  Error calling incentive API: " + e.getMessage());
            return 0.0f;
        }
    }
}