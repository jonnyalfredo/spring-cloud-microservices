package com.jonathanleite.clientapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Jonathan Leite")
                .email("jonathan@email.com")
                .document("12345678900")
                .build();

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jonathan Leite"))
                .andExpect(jsonPath("$.email").value("jonathan@email.com"))
                .andExpect(jsonPath("$.document").value("12345678900"));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Cliente Duplicado")
                .email("duplicado@email.com")
                .document("99999999999")
                .build();

        // cria o primeiro
        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        // tenta criar novamente
        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("") // inválido
                .email("email-invalido")
                .document("")
                .build();

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindClientById() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Cliente Busca")
                .email("busca@email.com")
                .document("11111111111")
                .build();

        String response = mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void shouldReturnAllClients() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldUpdateClientSuccessfully() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Cliente Update")
                .email("update@email.com")
                .document("22222222222")
                .build();

        String response = mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        ClientRequestDTO updateRequest = ClientRequestDTO.builder()
                .name("Cliente Atualizado")
                .email("update@email.com")
                .document("22222222222")
                .build();

        mockMvc.perform(
                        put("/clients/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente Atualizado"));
    }

    @Test
    void shouldDeleteClientSuccessfully() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Cliente Delete")
                .email("delete@email.com")
                .document("33333333333")
                .build();

        String response = mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/clients/{id}", id))
                .andExpect(status().isNoContent());
    }
}
