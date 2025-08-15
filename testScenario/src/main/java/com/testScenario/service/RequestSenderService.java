package com.testScenario.service;

import com.testScenario.model.AppResponse;
import com.testScenario.model.ClientRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface RequestSenderService {
    ResponseEntity<AppResponse<Long>> createOrderRequest(ClientRequest clientRequest);

    ResponseEntity<AppResponse<Long>> createOrderRequest(Map<String, Object> orderData);

    ResponseEntity<AppResponse<Long>> deactivateClientRequest(Long clientId);

    ResponseEntity<AppResponse<Long>> updateClientRequest(Long clientId);
}
