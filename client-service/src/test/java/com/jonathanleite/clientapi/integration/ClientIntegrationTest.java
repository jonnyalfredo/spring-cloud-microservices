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
                                .header("X-User-Id", "test-user")
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
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        // tenta criar novamente
        mockMvc.perform(
                        post("/clients")
                                .header("X-User-Id", "test-user")
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
                                .header("X-User-Id", "test-user")
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
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/clients/{id}", id)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void shouldReturnAllClients() throws Exception {
        mockMvc.perform(get("/clients")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldFilterClientsByNameEmailDocumentAndActiveWithPagination() throws Exception {
        ClientRequestDTO matchingClient = ClientRequestDTO.builder()
                .name("Maria Silva")
                .email("maria.filtro@email.com")
                .document("44444444444")
                .build();

        ClientRequestDTO otherClient = ClientRequestDTO.builder()
                .name("Joao Silva")
                .email("joao.filtro@email.com")
                .document("55555555555")
                .build();

        String matchingResponse = mockMvc.perform(
                        post("/clients")
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(matchingClient))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(
                        post("/clients")
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otherClient))
                )
                .andExpect(status().isCreated());

        Long matchingId = objectMapper.readTree(matchingResponse).get("id").asLong();

        mockMvc.perform(delete("/clients/{id}", matchingId)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clients")
                        .header("X-User-Id", "test-user")
                        .param("name", "maria")
                        .param("email", "maria.filtro@email.com")
                        .param("document", "44444444444")
                        .param("active", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(matchingId))
                .andExpect(jsonPath("$.content[0].name").value("Maria Silva"))
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));
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
                                .header("X-User-Id", "test-user")
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
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente Atualizado"));
    }

    @Test
    void shouldDeleteClientLogically() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .name("Cliente Delete")
                .email("delete@email.com")
                .document("33333333333")
                .build();

        String response = mockMvc.perform(
                        post("/clients")
                                .header("X-User-Id", "test-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/clients/{id}", id)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clients/{id}", id)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.active").value(false));
    }
}
