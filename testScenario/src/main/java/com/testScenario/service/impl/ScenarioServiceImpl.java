package com.testScenario.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.testScenario.model.AppResponse;
import com.testScenario.model.ClientRequest;
import com.testScenario.service.RequestSenderService;
import com.testScenario.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ScenarioServiceImpl implements ScenarioService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RequestSenderService requestSenderService;

    public ScenarioServiceImpl(RequestSenderService requestSenderService) {
        this.requestSenderService = requestSenderService;
    }

    @Override
    public String createClientsData() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            Long clientId = createClient(null);
            if (clientId == null) {
                logger.error("Failed to create client.");
                return "Failed to create client";
            } else {
                result.append(String.format("Client with %d created.\n", clientId));
            }
        }
        return result.toString();
    }

    @Override
    public String executeScenarioOne() {
        Faker faker = new Faker();
        String emailAddress = faker.internet().emailAddress();
        Map<String, String> client = Map.of(
                "name", "Test Client 1",
                "email", emailAddress,
                "phoneNumber", "212-658-3916"
        );
        Long clientId = createClient(null);

        if (clientId == null) {
            logger.error("Failed to create client for scenario 1.");
            return "Failed to create client for scenario 1.";
        }
        String idempotencyId = UUID.randomUUID().toString();
        Map<String, Object> orderData = Map.of(
                "idempotencyId", idempotencyId,
                "title", "Unique Order 1",
                "supplierId", clientId,
                "consumerId", clientId,
                "price", 10.0
        );

        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            logger.info("Attempt {}...", i);
            ResponseEntity<AppResponse<Long>> orderResponse = requestSenderService.createOrderRequest(orderData);
            AppResponse<Long> response = orderResponse.getBody();
            if (response != null && response.success()) {
                logger.info("Attempt {}: SUCCESS! Order created", i);
                result.append(String.format("Attempt %d: SUCCESS! Order created\n", i));
            } else {
                String message = mapResponse(response);
                logger.warn("Attempt {}: ERROR: {}", i, message);
                result.append(String.format("Attempt %d: ERROR: %s\n", i, message));
            }
        }
        return result.toString();
    }

    @Override
    public String runScenarioTwo() {
        Faker faker = new Faker();
        ClientRequest clientRequest = new ClientRequest("Test Client 2", faker.internet().emailAddress(), "212-658-3917");
        Long clientId = createClient(clientRequest);
        if (clientId == null) {
            logger.error("Failed to create client for scenario 2.");
            return "Failed to create client for scenario 2.";
        }

        Map<String, Object> firstOrderData = Map.of(
                "idempotencyId", UUID.randomUUID().toString(),
                "title", "Test Price Order",
                "supplierId", clientId,
                "consumerId", clientId,
                "price", BigDecimal.valueOf(-970)
        );

        ResponseEntity<AppResponse<Long>> updateClientResponse = requestSenderService.updateClientRequest(clientId);
        if (updateClientResponse == null || updateClientResponse.getBody() == null || !updateClientResponse.getBody().success()) {
            logger.error("Failed to update test client for scenario 2.");
            return "Failed to update test client for scenario 2.";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            BigDecimal price = BigDecimal.valueOf(100.0 - (i * 10.0));
            Map<String, Object> orderData = Map.of(
                    "idempotencyId", UUID.randomUUID().toString(),
                    "title", "Decreasing Price Order",
                    "supplierId", clientId,
                    "consumerId", clientId,
                    "price", price
            );
            logger.info("Attempt {} with price {}...", i + 1, price);
            DecimalFormat df = new DecimalFormat("#.00");
            String priceAsString = df.format(price);

            ResponseEntity<AppResponse<Long>> orderResponse = requestSenderService.createOrderRequest(orderData);
            AppResponse<Long> response = orderResponse.getBody();
            if (response != null && response.success()) {
                logger.info("Attempt {} (price {}): SUCCESS! Order created", i + 1, priceAsString);
                result.append(String.format("Attempt %d (price %s): SUCCESS! Order created\n", i + 1, priceAsString));
            } else {
                String message = mapResponse(response);
                result.append(String.format("Attempt %d (price %s): ERROR: %s\n", i + 1, priceAsString, message));
                logger.error("Attempt {} (price {}): ERROR: {}", i + 1, priceAsString, message);
            }
        }
        return result.toString();
    }

    @Override
    public String runScenarioThree() {
        logger.info("Scenario 3: Creating multiple orders while deactivating a client.");

        Long clientId = createClient(null);

        if (clientId == null) {
            logger.error("Failed to create client for scenario 3.");
            return "Failed to create client for scenario 3.";
        }

        int numberOfOrders = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfOrders + 1);
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < numberOfOrders; i++) {
            final int orderNum = i + 1;
            executor.submit(() -> {
                Map<String, Object> orderData = Map.of(
                        "idempotencyId", UUID.randomUUID().toString(),
                        "title", "Unique Order " + orderNum,
                        "supplierId", clientId,
                        "consumerId", clientId,
                        "price", BigDecimal.valueOf(50.0)
                );

                ResponseEntity<AppResponse<Long>> orderResponse = requestSenderService.createOrderRequest(orderData);
                AppResponse<Long> response = orderResponse.getBody();
                if (response != null && response.success()) {
                    result.append(String.format("Order %d: SUCCESS! Order created\n", orderNum));
                    logger.info("Order {}: SUCCESS! Order created", orderNum);
                } else {
                    String message = mapResponse(response);
                    result.append(String.format("Order %d: ERROR: %s\n", orderNum, message));
                    logger.error("Order {}: ERROR: {}", orderNum, message);
                }
            });
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.submit(() -> {
            ResponseEntity<AppResponse<Long>> deactivateClientResponse = requestSenderService.deactivateClientRequest(clientId);
            AppResponse<Long> response = deactivateClientResponse.getBody();
            if (response != null && response.success()) {
                result.append(String.format("Client with ID %d was successfully deactivated\n", clientId));
                logger.info("Client {} deactivated successfully", clientId);
            } else {
                String message = mapResponse(response);
                result.append(String.format("Error while deactivating client: %s\n", message));
                logger.error("Error deactivating client: {}", message);
            }
        });

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Error: {}", e.getMessage());
        }
        return result.toString();
    }

    public Long createClient(ClientRequest clientRequest) {
        ClientRequest request;
        if (clientRequest == null) {
            Faker faker = new Faker();
            request = new ClientRequest(
                    faker.name().fullName(),
                    faker.internet().emailAddress(),
                    "212-658-3917");
        } else {
            request = clientRequest;
        }

        ResponseEntity<AppResponse<Long>> response = requestSenderService.createOrderRequest(request);

        AppResponse<Long> body = response.getBody();
        if (body != null && body.success()) {
            logger.info("Client '{}' created successfully.", request.getName());
            return body.data();
        } else {
            logger.error("Error creating client '{}' with message: {}\n", request.getName(), body.message());
            return null;
        }
    }

    public String mapResponse(AppResponse<Long> appResponse) {
        if (appResponse == null || appResponse.message() == null) return "Response is EMPTY";

        String response = appResponse.message();
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;

        if (start >= 0 && end > start) {
            String jsonPart = response.substring(start, end);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonPart);
                return root.path("message").asText();
            } catch (Exception e) {
                logger.error("JSON parsing error: {}", e.getMessage());
                return "Response parsing error: " + e.getMessage();
            }
        } else {
            return "Invalid response";
        }
    }
}
