package com.orderapp.service;

import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.ClientRequest;
import com.orderapp.model.dto.ClientResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ClientService {

    AppResponse<Long> createClient(ClientRequest clientRequest);

    AppResponse<Page<ClientResponse>> getAllClients(int page, int size, String sortBy, String sortDir);

    AppResponse<ClientResponse> getClientById(Long clientId);

    AppResponse<Long> updateClientById(Long clientId, ClientRequest clientRequest);

    AppResponse<Long> deactivateClientById(Long clientId);

    Page<ClientResponse> searchClients(BigDecimal minProfit, BigDecimal maxProfit,
                                       String name, String email, String phoneNumber, Pageable pageable);

    @Transactional
    void resetProfits();

    AppResponse<Long> updateTestClientById(Long id);
}
