package com.orderapp.service.impl;

import com.orderapp.model.Client;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.ClientRequest;
import com.orderapp.model.dto.ClientResponse;
import com.orderapp.repository.ClientRepository;
import com.orderapp.repository.specification.ClientSpecification;
import com.orderapp.service.ClientService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.orderapp.utils.Constants.SUCCESS;
import static com.orderapp.utils.Utils.parseSortDirection;

@Service
public class ClientServiceImpl implements ClientService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${min.search.keyword.length}")
    private int minKeywordLength;

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public AppResponse<Long> createClient(ClientRequest clientRequest) {
        logger.info("Save a new client to DB");
        Client client = clientRepository.save(new Client(clientRequest));
        return AppResponse.ok(client.getId(), "Client was added successfully");
    }

    @Override
    public AppResponse<Page<ClientResponse>> getAllClients(int page, int size, String sortBy, String sortDir) {
        logger.info("Get all clients from DB");
        Pageable pageable = PageRequest.of(page, size, parseSortDirection(sortDir), sortBy);
        Page<ClientResponse> responsePage = clientRepository.findAll(pageable).map(ClientResponse::new);
        return AppResponse.ok(responsePage, SUCCESS);
    }

    @Override
    public AppResponse<ClientResponse> getClientById(Long clientId) {
        logger.info("Get client by ID: {}", clientId);
        Optional<Client> byIdWithOrders = clientRepository.findByIdWithOrders(clientId);
        if (byIdWithOrders.isEmpty()) {
            logger.warn("Client with Id : {} not found.", clientId);
            return AppResponse.fail("Client not found in DB.");
        }
        return AppResponse.ok(new ClientResponse(byIdWithOrders.get()), SUCCESS);
    }

    @Override
    public AppResponse<Long> updateClientById(Long clientId, ClientRequest clientRequest) {
        logger.info("Update client by ID: {}", clientId);
        Optional<Client> optionalClient = clientRepository.findJustClientById(clientId);
        if (optionalClient.isEmpty()) {
            logger.warn("Client with Id : {} not found.", clientId);
            return AppResponse.fail("Client not found in DB.");
        }
        Client client = optionalClient.get();
        client.setName(clientRequest.getName());
        client.setEmail(clientRequest.getEmail());
        client.setPhoneNumber(clientRequest.getPhoneNumber());
        if (Boolean.FALSE.equals(client.getActive()) && Boolean.TRUE.equals(clientRequest.getActive())) {
            client.setActive(clientRequest.getActive());
            client.setInactiveAt(null);
        }
        Client updated = clientRepository.save(client);
        return AppResponse.ok(updated.getId(), "Client was updated successfully");
    }

    @Override
    public AppResponse<Long> deactivateClientById(Long clientId) {
        logger.info("Deactivate client by ID: {}", clientId);
        Optional<Client> optionalClient = clientRepository.findJustClientById(clientId);
        if (optionalClient.isEmpty()) {
            logger.warn("Client with Id : {} not found.", clientId);
            return AppResponse.fail("Client not found in DB.");
        }
        Client client = optionalClient.get();
        client.setActive(false);
        client.setInactiveAt(LocalDateTime.now());
        Client deactivated = clientRepository.save(client);
        return AppResponse.ok(deactivated.getId(), "Client was deactivated successfully");
    }

    @Override
    public Page<ClientResponse> searchClients(BigDecimal minProfit, BigDecimal maxProfit,
                                              String name, String email, String phoneNumber, Pageable pageable) {
        logger.info("Get all clients from DB by parameters: minProfit - {}, maxProfit - {}, " +
                "name - {}, email - {}, phoneNumber - {}", minProfit, maxProfit, name, email, phoneNumber);
        Specification<Client> clientSpecification = ClientSpecification.filterClients(minProfit, maxProfit,
                name, email, phoneNumber, minKeywordLength);
        return clientRepository.findAll(clientSpecification, pageable)
                .map(ClientResponse::new);
    }

    @Transactional
    @Override
    public void resetProfits() {
        clientRepository.findAll().forEach(client -> {
            client.setProfit(BigDecimal.ZERO);
            clientRepository.save(client);
        });
    }

    @Override
    public AppResponse<Long> updateTestClientById(Long clientId) {
        logger.info("Update test client by ID: {}", clientId);
        Optional<Client> optionalClient = clientRepository.findJustClientById(clientId);
        if (optionalClient.isEmpty()) {
            logger.warn("Test client with Id : {} not found.", clientId);
            return AppResponse.fail("Test client not found in DB.");
        }
        Client client = optionalClient.get();
        client.setProfit(BigDecimal.valueOf(-970));
        Client updated = clientRepository.save(client);
        return AppResponse.ok(updated.getId(), "Test client was updated successfully");
    }
}
