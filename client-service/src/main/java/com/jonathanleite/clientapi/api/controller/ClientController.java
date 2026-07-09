package com.jonathanleite.clientapi.api.controller;

import com.jonathanleite.clientapi.api.dto.ClientPatchRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(
            @Valid @RequestBody ClientRequestDTO request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} created a client", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} updated client {}", userId, id);

        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> patch(
            @PathVariable Long id,
            @Valid @RequestBody ClientPatchRequestDTO request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} patched client {}", userId, id);

        return ResponseEntity.ok(clientService.patch(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ClientResponseDTO> activate(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} activated client {}", userId, id);

        return ResponseEntity.ok(clientService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ClientResponseDTO> deactivate(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} deactivated client {}", userId, id);

        return ResponseEntity.ok(clientService.deactivate(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> findById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} requested client {}", userId, id);

        return ResponseEntity.ok(clientService.findById(id));
    }

    @GetMapping("/public/test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Client API public endpoint OK");
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> findAll(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String document,
            Pageable pageable,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} requested client list", userId);

        return ResponseEntity.ok(
                clientService.findAll(email, document, pageable)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("User {} deleted client {}", userId, id);

        clientService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
