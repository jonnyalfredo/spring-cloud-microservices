package com.jonathanleite.clientapi.api.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.clientapi.api.controller.ClientController;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.exception.ConflictException;
import com.jonathanleite.clientapi.domain.exception.ResourceNotFoundException;
import com.jonathanleite.clientapi.domain.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Pageable;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- POST /clients ----------

    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("teste@email.com")
                .document("123")
                .name("Jonathan")
                .phone("11999999999")
                .build();

        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .email("teste@email.com")
                .document("123")
                .build();

        when(clientService.create(any(ClientRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("duplicado@email.com")
                .document("123")
                .name("Jonathan")
                .phone("11999999999")
                .build();

        when(clientService.create(any(ClientRequestDTO.class)))
                .thenThrow(new ConflictException("Email já cadastrado"));

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }


    // ---------- PUT /clients/{id} ----------

    @Test
    void shouldUpdateClientSuccessfully() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("novo@email.com")
                .document("456")
                .name("Jonathan")
                .phone("11999999999")
                .build();

        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .email("novo@email.com")
                .document("456")
                .build();

        when(clientService.update(eq(1L), any(ClientRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }


    // ---------- GET /clients/{id} ----------

    @Test
    void shouldFindClientByIdSuccessfully() throws Exception {
        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .email("teste@email.com")
                .document("123")
                .build();

        when(clientService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(clientService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        mockMvc.perform(get("/clients/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    // ---------- GET /clients ----------

    @Test
    void shouldFindAllClientsSuccessfully() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ClientResponseDTO> page = new PageImpl<>(
                List.of(
                        ClientResponseDTO.builder().id(1L).email("a@email.com").build(),
                        ClientResponseDTO.builder().id(2L).email("b@email.com").build()
                ),
                pageable,
                2
        );

        when(clientService.findAll(
                nullable(String.class),
                nullable(String.class),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/clients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }


    // ---------- DELETE /clients/{id} ----------

    @Test
    void shouldDeleteClientSuccessfully() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete("/clients/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
