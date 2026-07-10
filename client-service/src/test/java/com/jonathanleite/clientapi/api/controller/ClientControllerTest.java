package com.jonathanleite.clientapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.exception.ConflictException;
import com.jonathanleite.clientapi.domain.exception.ForbiddenException;
import com.jonathanleite.clientapi.domain.exception.ResourceNotFoundException;
import com.jonathanleite.clientapi.domain.exception.TechnicalException;
import com.jonathanleite.clientapi.domain.exception.UnauthorizedException;
import com.jonathanleite.clientapi.domain.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

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

        when(clientService.create(any(ClientRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/clients")
                        .header("X-User-Id", "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenRequestIsInvalid() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("email-invalido")
                .document("")
                .name("")
                .build();

        mockMvc.perform(post("/clients")
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-client-validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.path").value("/clients"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-validation"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    void shouldReturn400WhenRequiredHeaderIsMissing() throws Exception {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .email("teste@email.com")
                .document("123")
                .name("Jonathan")
                .phone("11999999999")
                .build();

        mockMvc.perform(post("/clients")
                        .header("X-Correlation-Id", "corr-client-header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-client-header"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Header obrigatório ausente: X-User-Id"))
                .andExpect(jsonPath("$.path").value("/clients"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-header"));
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
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-409")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Correlation-Id", "corr-client-409"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email já cadastrado"))
                .andExpect(jsonPath("$.path").value("/clients"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-409"));
    }

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

        when(clientService.update(eq(1L), any(ClientRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    @Test
    void shouldPatchClientSuccessfully() throws Exception {
        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .name("Jonathan Leite")
                .email("teste@email.com")
                .document("12345678900")
                .phone("11888888888")
                .build();

        when(clientService.patch(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jonathan Leite",
                                  "phone": "11888888888"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan Leite"))
                .andExpect(jsonPath("$.phone").value("11888888888"))
                .andExpect(jsonPath("$.email").value("teste@email.com"))
                .andExpect(jsonPath("$.document").value("12345678900"));
    }

    @Test
    void shouldReturn400WhenPatchEmailIsInvalid() throws Exception {
        mockMvc.perform(patch("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-patch-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email-invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-client-patch-email"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void shouldReturn400WhenPatchFieldIsBlank() throws Exception {
        mockMvc.perform(patch("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-patch-blank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-client-patch-blank"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void shouldReturn400WhenPatchDocumentIsInvalid() throws Exception {
        mockMvc.perform(patch("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-patch-document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "document": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-client-patch-document"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors[0].field").value("document"));
    }

    @Test
    void shouldActivateClientSuccessfully() throws Exception {
        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .active(true)
                .build();

        when(clientService.activate(1L)).thenReturn(response);

        mockMvc.perform(patch("/clients/{id}/activate", 1L)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn409WhenClientIsAlreadyActive() throws Exception {
        when(clientService.activate(1L))
                .thenThrow(new ConflictException("Cliente já está ativo"));

        mockMvc.perform(patch("/clients/{id}/activate", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-active"))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Correlation-Id", "corr-client-active"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cliente já está ativo"));
    }

    @Test
    void shouldDeactivateClientSuccessfully() throws Exception {
        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .active(false)
                .build();

        when(clientService.deactivate(1L)).thenReturn(response);

        mockMvc.perform(patch("/clients/{id}/deactivate", 1L)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturn409WhenClientIsAlreadyInactive() throws Exception {
        when(clientService.deactivate(1L))
                .thenThrow(new ConflictException("Cliente já está inativo"));

        mockMvc.perform(patch("/clients/{id}/deactivate", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-inactive"))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Correlation-Id", "corr-client-inactive"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cliente já está inativo"));
    }

    @Test
    void shouldFindClientByIdSuccessfully() throws Exception {
        ClientResponseDTO response = ClientResponseDTO.builder()
                .id(1L)
                .email("teste@email.com")
                .document("123")
                .build();

        when(clientService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(clientService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        mockMvc.perform(get("/clients/{id}", 99L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-404"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Correlation-Id", "corr-client-404"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"))
                .andExpect(jsonPath("$.path").value("/clients/99"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-404"));
    }

    @Test
    void shouldReturn401WhenUnauthorized() throws Exception {
        when(clientService.findById(1L)).thenThrow(new UnauthorizedException("Nao autorizado"));

        mockMvc.perform(get("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Correlation-Id", "corr-client-401"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Nao autorizado"))
                .andExpect(jsonPath("$.path").value("/clients/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-401"));
    }

    @Test
    void shouldReturn403WhenForbidden() throws Exception {
        when(clientService.findById(1L)).thenThrow(new ForbiddenException("Acesso negado"));

        mockMvc.perform(get("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-403"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Correlation-Id", "corr-client-403"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.path").value("/clients/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-403"));
    }

    @Test
    void shouldReturn500WhenTechnicalErrorHappens() throws Exception {
        when(clientService.findById(1L)).thenThrow(new TechnicalException("Falha tecnica"));

        mockMvc.perform(get("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user")
                        .header("X-Correlation-Id", "corr-client-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Correlation-Id", "corr-client-500"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Falha tecnica"))
                .andExpect(jsonPath("$.path").value("/clients/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-client-500"));
    }

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
                nullable(String.class),
                nullable(Boolean.class),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/clients")
                        .header("X-User-Id", "test-user")
                        .param("name", "Cliente")
                        .param("email", "a@email.com")
                        .param("document", "12345678900")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(clientService).findAll(
                eq("Cliente"),
                eq("a@email.com"),
                eq("12345678900"),
                eq(true),
                any(Pageable.class)
        );
    }

    @Test
    void shouldDeleteClientLogically() throws Exception {
        mockMvc.perform(delete("/clients/{id}", 1L)
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isNoContent());
    }
}
