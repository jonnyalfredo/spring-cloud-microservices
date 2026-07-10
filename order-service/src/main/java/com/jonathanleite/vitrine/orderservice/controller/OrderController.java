package com.jonathanleite.vitrine.orderservice.controller;

import com.jonathanleite.vitrine.orderservice.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import com.jonathanleite.vitrine.orderservice.exception.ApiErrorResponse;
import com.jonathanleite.vitrine.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Operações para criação, consulta e controle de status de pedidos")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/public/test")
    @Operation(
            summary = "Testar endpoint público",
            description = "Retorna uma mensagem simples para validar se o order-service está respondendo."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Serviço disponível",
            content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject(value = "Order API public endpoint OK")
            )
    )
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Order API public endpoint OK");
    }

    @PostMapping
    @Operation(
            summary = "Criar pedido",
            description = "Cria um pedido com status inicial CREATED. Antes de salvar, o serviço consulta o client-service via Feign para validar se o cliente existe e está ativo."
    )
    @Parameter(
            name = "X-User-Id",
            in = ParameterIn.HEADER,
            description = "Header interno usado na integração com o client-service. O order-service envia o valor fixo 'order-service' ao consultar clientes; consumidores externos não precisam informar este header.",
            example = "order-service"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "clientId": 10,
                                      "description": "Compra de notebook",
                                      "amount": 3500.00,
                                      "status": "CREATED",
                                      "createdAt": "2026-07-09T16:30:00",
                                      "updatedAt": "2026-07-09T16:30:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou cliente inativo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Erro de validação",
                                            value = """
                                                    {
                                                      "timestamp": "2026-07-09T16:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Erro de validação",
                                                      "path": "/orders",
                                                      "correlationId": "demo-order-001",
                                                      "errors": [
                                                        {
                                                          "field": "amount",
                                                          "message": "O valor deve ser maior que zero"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Cliente inativo",
                                            value = """
                                                    {
                                                      "timestamp": "2026-07-09T16:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Cliente está inativo",
                                                      "path": "/orders",
                                                      "correlationId": "demo-order-002"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-09T16:30:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Cliente não encontrado",
                                      "path": "/orders",
                                      "correlationId": "demo-order-003"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Client-service indisponível",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-09T16:30:00",
                                      "status": 503,
                                      "error": "Service Unavailable",
                                      "message": "Client-service indisponível. Não foi possível validar o cliente no momento.",
                                      "path": "/orders",
                                      "correlationId": "demo-order-004"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody(
                    description = "Dados do pedido a ser criado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "clientId": 10,
                                      "description": "Compra de notebook",
                                      "amount": 3500.00
                                    }
                                    """)
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderRequestDTO request) {
        log.info("Recebida requisição para criar pedido clientId={}", request.getClientId());

        OrderResponseDTO response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna os dados de um pedido cadastrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID do pedido", example = "1", required = true)
            @PathVariable Long id) {
        log.info("Buscando pedido id={}", id);

        OrderResponseDTO response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos",
            description = "Lista pedidos com paginação e ordenação usando os parâmetros padrão do Spring: page, size e sort."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Página de pedidos retornada com sucesso",
            content = @Content(mediaType = "application/json")
    )
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        log.info("Listando pedidos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Atualizar status do pedido",
            description = "Atualiza o status respeitando o fluxo permitido: CREATED -> PROCESSING, PROCESSING -> COMPLETED, CREATED -> CANCELLED e PROCESSING -> CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "clientId": 10,
                                      "description": "Compra de notebook",
                                      "amount": 3500.00,
                                      "status": "PROCESSING",
                                      "createdAt": "2026-07-09T16:30:00",
                                      "updatedAt": "2026-07-09T16:45:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Transição de status inválida ou pedido já finalizado/cancelado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-09T16:30:00",
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Transição inválida de CREATED para COMPLETED",
                                      "path": "/orders/1/status",
                                      "correlationId": "demo-order-status-001"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @Parameter(description = "ID do pedido", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(
                    description = "Novo status do pedido",
                    example = "PROCESSING",
                    required = true,
                    schema = @Schema(implementation = OrderStatus.class)
            )
            @RequestParam OrderStatus status) {

        log.info("Atualizando status do pedido id={} para {}", id, status);

        OrderResponseDTO response = orderService.updateStatus(id, status);

        return ResponseEntity.ok(response);
    }
}
