package com.orderapp.unit;

import com.orderapp.model.Client;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.ClientRequest;
import com.orderapp.model.dto.ClientResponse;
import com.orderapp.repository.ClientRepository;
import com.orderapp.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequest clientRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clientService, "minKeywordLength", 3);

        client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhoneNumber("123-456-7890");
        client.setActive(true);

        clientRequest = new ClientRequest();
        clientRequest.setName("Jane Doe");
        clientRequest.setEmail("jane.doe@example.com");
        clientRequest.setPhoneNumber("987-654-3210");
        clientRequest.setActive(true);
    }

    @Test
    void createClient_success() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        AppResponse<Long> response = clientService.createClient(clientRequest);

        assertTrue(response.success());
        assertEquals(1L, response.data());
        assertEquals("Client was added successfully", response.message());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void getAllClients_success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), pageable, 1);

        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        AppResponse<Page<ClientResponse>> response = clientService.getAllClients(0, 10, "id", "asc");

        assertTrue(response.success());
        assertFalse(response.data().isEmpty());
        assertEquals(1, response.data().getTotalElements());
        assertEquals("success", response.message());

        verify(clientRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getClientById_success() {
        when(clientRepository.findByIdWithOrders(1L)).thenReturn(Optional.of(client));

        AppResponse<ClientResponse> response = clientService.getClientById(1L);

        assertTrue(response.success());
        assertEquals("John Doe", response.data().getName());
        assertEquals("success", response.message());

        verify(clientRepository, times(1)).findByIdWithOrders(1L);
    }

    @Test
    void getClientById_notFound() {
        when(clientRepository.findByIdWithOrders(anyLong())).thenReturn(Optional.empty());

        AppResponse<ClientResponse> response = clientService.getClientById(99L);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Client not found in DB.", response.message());

        verify(clientRepository, times(1)).findByIdWithOrders(99L);
    }

    @Test
    void updateClientById_success() {
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        AppResponse<Long> response = clientService.updateClientById(1L, clientRequest);

        assertTrue(response.success());
        assertEquals(1L, response.data());
        assertEquals("Client was updated successfully", response.message());

        verify(clientRepository, times(1)).findJustClientById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientById_successMakingClientActive() {
        client.setActive(Boolean.FALSE);
        clientRequest.setActive(Boolean.TRUE);

        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        AppResponse<Long> response = clientService.updateClientById(1L, clientRequest);

        assertTrue(response.success());
        assertEquals(1L, response.data());
        assertEquals("Client was updated successfully", response.message());

        verify(clientRepository, times(1)).findJustClientById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientById_notFound() {
        when(clientRepository.findJustClientById(anyLong())).thenReturn(Optional.empty());

        AppResponse<Long> response = clientService.updateClientById(99L, clientRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Client not found in DB.", response.message());
        verify(clientRepository, times(1)).findJustClientById(99L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deactivateClientById_success() {
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        AppResponse<Long> response = clientService.deactivateClientById(1L);

        assertTrue(response.success());
        assertEquals(1L, response.data());
        assertEquals("Client was deactivated successfully", response.message());
        assertFalse(client.getActive());
        assertNotNull(client.getInactiveAt());

        verify(clientRepository, times(1)).findJustClientById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void deactivateClientById_notFound() {
        when(clientRepository.findJustClientById(anyLong())).thenReturn(Optional.empty());

        AppResponse<Long> response = clientService.deactivateClientById(99L);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Client not found in DB.", response.message());

        verify(clientRepository, times(1)).findJustClientById(99L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void searchClients_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), pageable, 1);

        when(clientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(clientPage);

        Page<ClientResponse> responsePage = clientService.searchClients(
                null, null, "John", null, null, pageable);

        assertFalse(responsePage.isEmpty());
        assertEquals(1, responsePage.getTotalElements());
        verify(clientRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchClients_noResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> emptyPage = Page.empty(pageable);

        when(clientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<ClientResponse> responsePage = clientService.searchClients(
                null, null, "NonExistent", null, null, pageable);

        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());
        verify(clientRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
}
