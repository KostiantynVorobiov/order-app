package com.orderapp.integration;

import com.orderapp.model.Client;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.ClientRequest;
import com.orderapp.model.dto.ClientResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.repository.ClientRepository;
import com.orderapp.repository.OrderRepository;
import com.orderapp.service.ClientService;
import com.orderapp.service.OrderService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private OrderRepository orderRepository;

    private ClientRequest clientRequest;
    private OrderRequest orderRequest;
    private Client supplier;
    private Client consumer;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void createClient_shouldSaveNewClientAndReturnSuccess() {
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("John Doe");
        clientRequest.setEmail("john.doe@example.com");
        clientRequest.setPhoneNumber("123-456-7890");

        AppResponse<Long> response = clientService.createClient(clientRequest);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();

        Optional<Client> savedClient = clientRepository.findById(response.data());
        assertTrue(savedClient.isPresent());
        assertThat(savedClient.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void getAllClients_shouldReturnPaginatedAndSortedClients() {
        createTestClients();
        AppResponse<Page<ClientResponse>> response = clientService.getAllClients(0, 2, "name", "asc");

        assertThat(response.success()).isTrue();
        Page<ClientResponse> clientsPage = response.data();

        assertThat(clientsPage.getTotalElements()).isEqualTo(4);
        assertThat(clientsPage.getTotalPages()).isEqualTo(2);
        assertThat(clientsPage.getContent()).hasSize(2);

        List<ClientResponse> clientList = clientsPage.getContent();
        assertThat(clientList.get(0).getName()).isEqualTo("Anna Banana");
        assertThat(clientList.get(1).getName()).isEqualTo("Jane Smith");
    }

    @Test
    void getAllClients_shouldReturnCorrectPageWhenRequestingSecondPage() {
        createTestClients();
        AppResponse<Page<ClientResponse>> response = clientService.getAllClients(1, 2, "name", "asc");

        assertThat(response.success()).isTrue();
        Page<ClientResponse> clientsPage = response.data();

        assertThat(clientsPage.getContent()).hasSize(2);
        assertThat(clientsPage.getContent().get(0).getName()).isEqualTo("John Doe");
        assertThat(clientsPage.getContent().get(1).getName()).isEqualTo("Johnny Appleseed");
    }

    @Test
    void getClientById_shouldReturnClientWhenClientExists() {
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("Jane Smith");
        clientRequest.setEmail("jane.smith@example.com");
        clientRequest.setPhoneNumber("098-765-4321");
        Client savedClient = clientRepository.save(new Client(clientRequest));

        AppResponse<ClientResponse> response = clientService.getClientById(savedClient.getId());

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();
        assertThat(response.data().getName()).isEqualTo("Jane Smith");
    }

    @Test
    void getClientById_shouldReturnFailureWhenClientDoesNotExist() {
        AppResponse<ClientResponse> response = clientService.getClientById(999L);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Client not found in DB.");
    }

    @Test
    void updateClientById_shouldUpdateClientAndReturnSuccess() {
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("Old Name");
        clientRequest.setEmail("old@example.com");
        clientRequest.setPhoneNumber("111-222-3333");
        Client savedClient = clientRepository.save(new Client(clientRequest));

        ClientRequest updateRequest = new ClientRequest();
        updateRequest.setName("New Name");
        updateRequest.setEmail("new@example.com");
        updateRequest.setPhoneNumber("444-555-6666");
        updateRequest.setActive(true);

        AppResponse<Long> response = clientService.updateClientById(savedClient.getId(), updateRequest);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo(savedClient.getId());

        Optional<Client> updatedClient = clientRepository.findById(savedClient.getId());
        assertTrue(updatedClient.isPresent());
        assertThat(updatedClient.get().getName()).isEqualTo("New Name");
        assertThat(updatedClient.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateClientById_shouldReturnFailureWhenClientDoesNotExist() {
        long nonExistentClientId = 999L;
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("New Name");
        clientRequest.setEmail("new.email@test.com");
        clientRequest.setPhoneNumber("111-222-3333");

        AppResponse<Long> response = clientService.updateClientById(nonExistentClientId, clientRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Client not found in DB.");
        assertThat(response.data()).isNull();
    }

    @Test
    void updateClientById_shouldReactivateClientWhenStatusIsChanged() {
        Client deactivatedClient = new Client("Inactive Client", "inactive@test.com", "987-654-3210");
        deactivatedClient.setActive(false);
        deactivatedClient.setInactiveAt(LocalDateTime.now().minusDays(1));
        deactivatedClient = clientRepository.save(deactivatedClient);

        ClientRequest updateRequest = new ClientRequest();
        updateRequest.setName("Active Client Again");
        updateRequest.setEmail("active@test.com");
        updateRequest.setPhoneNumber("987-654-3210");
        updateRequest.setActive(true);

        AppResponse<Long> response = clientService.updateClientById(deactivatedClient.getId(), updateRequest);

        assertThat(response.success()).isTrue();

        Optional<Client> updatedClientOptional = clientRepository.findById(deactivatedClient.getId());
        assertThat(updatedClientOptional).isPresent();
        Client updatedClient = updatedClientOptional.get();

        assertThat(updatedClient.getActive()).isTrue();
        assertThat(updatedClient.getInactiveAt()).isNull();

        assertThat(updatedClient.getName()).isEqualTo("Active Client Again");
        assertThat(updatedClient.getEmail()).isEqualTo("active@test.com");
    }

    @Test
    void deactivateClientById_shouldDeactivateClientAndReturnSuccess() {
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("Active Client");
        clientRequest.setEmail("active@example.com");
        clientRequest.setPhoneNumber("123-456-7890");
        Client savedClient = clientRepository.save(new Client(clientRequest));

        AppResponse<Long> response = clientService.deactivateClientById(savedClient.getId());

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo(savedClient.getId());

        Optional<Client> deactivatedClient = clientRepository.findById(savedClient.getId());
        assertTrue(deactivatedClient.isPresent());
        assertFalse(deactivatedClient.get().getActive());
        assertThat(deactivatedClient.get().getInactiveAt()).isNotNull();
    }

    @Test
    void deactivateClientById_shouldReturnFailureWhenClientDoesNotExist() {
        long nonExistentClientId = 999L;

        AppResponse<Long> response = clientService.deactivateClientById(nonExistentClientId);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Client not found in DB.");
        assertThat(response.data()).isNull();
    }

    @Test
    void searchClients_shouldFilterByAllParameters() {
        createTestClients();
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minProfit = new BigDecimal("1000.00");
        BigDecimal maxProfit = new BigDecimal("2000.00");

        Page<ClientResponse> responsePage = clientService.searchClients(
                minProfit, maxProfit, "John", "john.doe@email.com", "111-222-3333", pageable);

        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).getName()).isEqualTo("John Doe");
        assertThat(responsePage.getContent().get(0).getEmail()).isEqualTo("john.doe@email.com");
    }

    @Test
    void searchClients_shouldFilterByNameWhenOnlyNameProvided() {
        createTestClients();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ClientResponse> responsePage = clientService.searchClients(
                null, null, "John", null, null, pageable);

        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent().stream().map(ClientResponse::getName))
                .containsExactlyInAnyOrder("John Doe", "Johnny Appleseed");
    }

    @Test
    void searchClients_shouldFilterByEmailWhenEmailLikeProvided() {
        createTestClients();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ClientResponse> responsePage = clientService.searchClients(
                null, null, null, "ane", null, pageable);

        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent().stream().map(ClientResponse::getName))
                .containsExactlyInAnyOrder("Jane Smith", "Anna Banana");
    }

    @Test
    void searchClients_shouldFilterByProfitRangeWhenOnlyProfitProvided() {
        createTestClients();
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minProfit = new BigDecimal("500.00");
        BigDecimal maxProfit = new BigDecimal("2000.00");

        Page<ClientResponse> responsePage = clientService.searchClients(
                minProfit, maxProfit, null, null, null, pageable);

        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent().stream().map(ClientResponse::getName))
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void searchClients_shouldReturnAllClientsWhenNoParametersProvided() {
        createTestClients();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ClientResponse> responsePage = clientService.searchClients(
                null, null, null, null, null, pageable);

        assertThat(responsePage.getContent()).hasSize(4);
    }

    @Test
    void searchClients_shouldThrowException_whenEmailIsTooShort() {
        int minKeywordLength = 3;
        Pageable pageable = PageRequest.of(0, 10);

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            clientService.searchClients(
                    null, null, null, "ab", null, pageable);
        });

        String expectedMessage = "email parameter should have a minimum of " + minKeywordLength + " characters.";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    private void createTestClients() {
        Client client1 = new Client("John Doe", "john.doe@email.com", "111-222-3333");
        client1.setProfit(new BigDecimal("1500.00"));
        clientRepository.save(client1);

        Client client2 = new Client("Jane Smith", "jane.smith@email.com", "444-555-6666");
        client2.setProfit(new BigDecimal("500.00"));
        clientRepository.save(client2);

        Client client3 = new Client("Johnny Appleseed", "johnny@email.com", "999-888-7777");
        client3.setProfit(new BigDecimal("2500.00"));
        clientRepository.save(client3);

        Client client4 = new Client("Anna Banana", "anna.banane@test.com", "111-222-0000");
        client4.setProfit(new BigDecimal("100.00"));
        clientRepository.save(client4);
    }
}
