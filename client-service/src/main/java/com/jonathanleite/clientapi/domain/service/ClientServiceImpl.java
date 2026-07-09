package com.jonathanleite.clientapi.domain.service;

import com.jonathanleite.clientapi.api.dto.ClientPatchRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.entity.Client;
import com.jonathanleite.clientapi.domain.exception.BusinessException;
import com.jonathanleite.clientapi.domain.exception.ConflictException;
import com.jonathanleite.clientapi.domain.exception.ResourceNotFoundException;
import com.jonathanleite.clientapi.domain.exception.ValidationException;
import com.jonathanleite.clientapi.domain.repository.ClientRepository;
import com.jonathanleite.clientapi.domain.repository.specification.ClientSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("\\d{11}|\\d{14}");

    private final ClientRepository clientRepository;

    @Override
    public ClientResponseDTO create(ClientRequestDTO request) {
        validateDuplicateClient(request);

        Client client = Client.builder()
                .name(request.getName())
                .document(request.getDocument())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        return toResponseDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO update(Long id, ClientRequestDTO request) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente não encontrado"));

        validateDuplicateClientUpdate(id, request);

        client.setName(request.getName());
        client.setDocument(request.getDocument());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());

        return toResponseDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente não encontrado"));

        return toResponseDTO(client);
    }

    @Override
    public Page<ClientResponseDTO> findAll(
            String email,
            String document,
            Pageable pageable) {

        Specification<Client> specification = Specification
                .where(ClientSpecification.hasEmail(email))
                .and(ClientSpecification.hasDocument(document));

        return clientRepository.findAll(specification, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    public void delete(Long id) {

        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado");
        }

        clientRepository.deleteById(id);
    }

    /* ===============================
       VALIDAÇÕES DE NEGÓCIO
       =============================== */

    private void validateDuplicateClient(ClientRequestDTO request) {

        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email já cadastrado");
        }

        if (clientRepository.existsByDocument(request.getDocument())) {
            throw new ConflictException("Documento já cadastrado");
        }
    }

    private void validateDuplicateClientUpdate(Long id, ClientRequestDTO request) {

        clientRepository.findByEmail(request.getEmail())
                .filter(client -> !client.getId().equals(id))
                .ifPresent(client -> {
                    throw new BusinessException("Email já cadastrado");
                });

        clientRepository.findByDocument(request.getDocument())
                .filter(client -> !client.getId().equals(id))
                .ifPresent(client -> {
                    throw new BusinessException("Documento já cadastrado");
                });
    }

    @Override
    public ClientResponseDTO patch(Long id, ClientPatchRequestDTO request) {

        validatePatchRequest(request);

        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente não encontrado"));

        // Validação de duplicidade SOMENTE se o campo vier preenchido
        if (request.getEmail() != null) {
            clientRepository.findByEmail(request.getEmail())
                    .filter(c -> !c.getId().equals(id))
                    .ifPresent(c -> {
                        throw new BusinessException("Email já cadastrado");
                    });
            client.setEmail(request.getEmail());
        }

        if (request.getDocument() != null) {
            clientRepository.findByDocument(request.getDocument())
                    .filter(c -> !c.getId().equals(id))
                    .ifPresent(c -> {
                        throw new BusinessException("Documento já cadastrado");
                    });
            client.setDocument(request.getDocument());
        }

        if (request.getName() != null) {
            client.setName(request.getName());
        }

        if (request.getPhone() != null) {
            client.setPhone(request.getPhone());
        }

        return toResponseDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO activate(Long id) {
        Client client = findClientOrThrow(id);

        if (Boolean.TRUE.equals(client.getActive())) {
            throw new ConflictException("Cliente já está ativo");
        }

        client.setActive(true);

        return toResponseDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO deactivate(Long id) {
        Client client = findClientOrThrow(id);

        if (Boolean.FALSE.equals(client.getActive())) {
            throw new ConflictException("Cliente já está inativo");
        }

        client.setActive(false);

        return toResponseDTO(clientRepository.save(client));
    }

    private Client findClientOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente não encontrado"));
    }

    private void validatePatchRequest(ClientPatchRequestDTO request) {
        validateOptionalText("Nome", request.getName());
        validateOptionalText("Email", request.getEmail());
        validateOptionalText("Documento", request.getDocument());
        validateOptionalText("Telefone", request.getPhone());

        if (request.getEmail() != null && !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new ValidationException("Email inválido");
        }

        if (request.getDocument() != null && !DOCUMENT_PATTERN.matcher(request.getDocument()).matches()) {
            throw new ValidationException("Documento deve conter 11 ou 14 dígitos");
        }
    }

    private void validateOptionalText(String fieldName, String value) {
        if (value != null && value.isBlank()) {
            throw new ValidationException(fieldName + " não pode ser vazio");
        }
    }

    private ClientResponseDTO toResponseDTO(Client client) {

        return ClientResponseDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .document(client.getDocument())
                .email(client.getEmail())
                .phone(client.getPhone())
                .active(client.getActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
