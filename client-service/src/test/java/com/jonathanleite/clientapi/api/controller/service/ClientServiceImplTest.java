package com.jonathanleite.clientapi.api.controller.service;

import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.entity.Client;
import com.jonathanleite.clientapi.domain.exception.ConflictException;
import com.jonathanleite.clientapi.domain.exception.ResourceNotFoundException;
import com.jonathanleite.clientapi.domain.repository.ClientRepository;
import com.jonathanleite.clientapi.domain.service.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    /* ===============================
       CREATE
       =============================== */

    @Test
    void shouldCreateClientSuccessfully() {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Jonathan")
                .email("jonathan@email.com")
                .document("12345678900")
                .phone("11999999999")
                .build();

        when(clientRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(clientRepository.existsByDocument(request.getDocument())).thenReturn(false);
        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponseDTO response = clientService.create(request);

        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("duplicado@email.com")
                .document("123")
                .build();

        when(clientRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> clientService.create(request));
    }

    /* ===============================
       FIND BY ID
       =============================== */

    @Test
    void shouldFindClientById() {
        Client client = Client.builder()
                .id(1L)
                .name("Cliente")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        ClientResponseDTO response = clientService.findById(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void shouldThrowExceptionWhenClientNotFound() {
        when(clientRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> clientService.findById(99L));
    }

    /* ===============================
       FIND ALL (FILTROS + PAGINAÇÃO)
       =============================== */

    @Test
    void shouldReturnPagedClientListWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Client> page = new PageImpl<>(
                List.of(new Client(), new Client()),
                pageable,
                2
        );

        when(clientRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<ClientResponseDTO> result =
                clientService.findAll(null, null, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    /* ===============================
       DELETE
       =============================== */

    @Test
    void shouldDeleteClientSuccessfully() {
        when(clientRepository.existsById(1L)).thenReturn(true);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingClient() {
        when(clientRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> clientService.delete(1L));
    }
}
