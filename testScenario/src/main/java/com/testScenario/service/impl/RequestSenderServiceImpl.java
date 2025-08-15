package com.testScenario.service.impl;

import com.testScenario.model.AppResponse;
import com.testScenario.model.ClientRequest;
import com.testScenario.service.RequestSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RequestSenderServiceImpl implements RequestSenderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${order.api.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public RequestSenderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<AppResponse<Long>> createOrderRequest(ClientRequest clientRequest) {
        try {
            HttpEntity<ClientRequest> requestEntity = new HttpEntity<>(clientRequest);

            ResponseEntity<AppResponse<Long>> response = restTemplate.exchange(
                    baseUrl + "/client",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<AppResponse<Long>>() {
                    });
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(AppResponse.fail("HTTP error: " + e.getMessage()));
        } catch (ResourceAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AppResponse.fail("Service unavailable: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppResponse.fail("Internal error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<AppResponse<Long>> createOrderRequest(Map<String, Object> orderData) {
        try {
            ResponseEntity<AppResponse<Long>> response = restTemplate.exchange(
                    baseUrl + "/order",
                    HttpMethod.POST,
                    new HttpEntity<>(orderData),
                    new ParameterizedTypeReference<AppResponse<Long>>() {
                    }
            );
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(AppResponse.fail("HTTP error: " + e.getMessage()));
        } catch (ResourceAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AppResponse.fail("Service unavailable: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppResponse.fail("Internal error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<AppResponse<Long>> deactivateClientRequest(Long clientId) {
        String deactivateUrl = baseUrl + "/client/" + clientId + "/deactivate";
        try {
            return restTemplate.exchange(
                    deactivateUrl,
                    HttpMethod.PATCH,
                    null,
                    new ParameterizedTypeReference<AppResponse<Long>>() {
                    }
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(AppResponse.fail("HTTP error: " + e.getMessage()));
        } catch (ResourceAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AppResponse.fail("Service unavailable: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppResponse.fail("Internal error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<AppResponse<Long>> updateClientRequest(Long clientId) {
        String updateUrl = baseUrl + "/client/" + clientId + "/test-update";
        try {
            return restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PATCH,
                    null,
                    new ParameterizedTypeReference<AppResponse<Long>>() {
                    }
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(AppResponse.fail("HTTP error: " + e.getMessage()));
        } catch (ResourceAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AppResponse.fail("Service unavailable: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppResponse.fail("Internal error: " + e.getMessage()));
        }
    }
}
