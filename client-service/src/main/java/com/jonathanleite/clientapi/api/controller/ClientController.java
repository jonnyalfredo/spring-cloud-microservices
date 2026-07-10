package com.jonathanleite.clientapi.api.controller;

import com.jonathanleite.clientapi.api.dto.ClientPatchRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.api.exception.ApiErrorResponse;
import com.jonathanleite.clientapi.domain.service.ClientService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Clientes",
        description = "Operações de cadastro, consulta, atualização e inativação lógica de clientes"
)
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(
            summary = "Criar cliente",
            description = "Cria um cliente ativo. Email e documento devem ser únicos.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados do cliente que será criado",
                    content = @Content(
                            schema = @Schema(implementation = ClientRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Cliente novo",
                                    value = """
                                            {
                                              "name": "Maria Silva",
                                              "document": "12345678900",
                                              "email": "maria.silva@email.com",
                                              "phone": "11999999999"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> create(
            @Valid @RequestBody ClientRequestDTO request,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} created a client", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar cliente",
            description = "Atualiza todos os dados editáveis do cliente. O campo active não é alterado por este endpoint.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Novos dados completos do cliente",
                    content = @Content(
                            schema = @Schema(implementation = ClientRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Cliente atualizado",
                                    value = """
                                            {
                                              "name": "Maria Souza",
                                              "document": "12345678900",
                                              "email": "maria.souza@email.com",
                                              "phone": "11888888888"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado em outro cliente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> update(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO request,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} updated client {}", userId, id);

        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualizar cliente parcialmente",
            description = "Atualiza apenas os campos enviados. Campos ausentes são ignorados e strings vazias são bloqueadas.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Campos do cliente que devem ser alterados",
                    content = @Content(
                            schema = @Schema(implementation = ClientPatchRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Alteração parcial",
                                    value = """
                                            {
                                              "email": "maria.novo@email.com",
                                              "phone": "11777777777"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado em outro cliente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> patch(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClientPatchRequestDTO request,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} patched client {}", userId, id);

        return ResponseEntity.ok(clientService.patch(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Ativar cliente",
            description = "Ativa um cliente inativo e retorna o cliente atualizado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente ativado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Cliente já está ativo",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> activate(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} activated client {}", userId, id);

        return ResponseEntity.ok(clientService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Inativar cliente",
            description = "Inativa um cliente ativo e retorna o cliente atualizado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente inativado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Cliente já está inativo",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> deactivate(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} deactivated client {}", userId, id);

        return ResponseEntity.ok(clientService.deactivate(id));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados de um cliente cadastrado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ClientResponseDTO> findById(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} requested client {}", userId, id);

        return ResponseEntity.ok(clientService.findById(id));
    }

    @GetMapping("/public/test")
    @Operation(
            summary = "Testar disponibilidade pública",
            description = "Endpoint público simples usado para validar se o serviço está respondendo."
    )
    @ApiResponse(responseCode = "200", description = "Serviço disponível")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Client API public endpoint OK");
    }

    @GetMapping
    @Operation(
            summary = "Listar clientes",
            description = "Lista clientes com paginação e filtros opcionais por nome, email, documento e status ativo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de clientes retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Header obrigatório ausente ou parâmetro inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Page<ClientResponseDTO>> findAll(
            @Parameter(description = "Busca parcial por nome, ignorando maiúsculas e minúsculas", example = "maria")
            @RequestParam(required = false) String name,
            @Parameter(description = "Busca exata por email", example = "maria.silva@email.com")
            @RequestParam(required = false) String email,
            @Parameter(description = "Busca exata por CPF ou CNPJ", example = "12345678900")
            @RequestParam(required = false) String document,
            @Parameter(description = "Filtra clientes ativos ou inativos", example = "true")
            @RequestParam(required = false) Boolean active,
            @ParameterObject
            Pageable pageable,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} requested client list", userId);

        return ResponseEntity.ok(
                clientService.findAll(name, email, document, active, pageable)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir cliente logicamente",
            description = "Inativa o cliente sem remover o registro do banco. A operação é idempotente para clientes já inativos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente inativado logicamente"),
            @ApiResponse(responseCode = "400", description = "Header obrigatório ausente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id,
            @Parameter(
                    name = "X-User-Id",
                    description = "Identificador do usuário que executa a operação",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-user"
            )
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} logically deleted client {}", userId, id);

        clientService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
