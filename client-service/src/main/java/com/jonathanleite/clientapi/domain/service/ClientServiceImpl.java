package com.jonathanleite.clientapi.domain.service;

import com.jonathanleite.clientapi.api.dto.ClientPatchRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import com.jonathanleite.clientapi.domain.entity.Client;
import com.jonathanleite.clientapi.domain.exception.BusinessException;
import com.jonathanleite.clientapi.domain.exception.ConflictException;
import com.jonathanleite.clientapi.domain.exception.ResourceNotFoundException;
import com.jonathanleite.clientapi.domain.repository.ClientRepository;
import com.jonathanleite.clientapi.domain.repository.specification.ClientSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

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
