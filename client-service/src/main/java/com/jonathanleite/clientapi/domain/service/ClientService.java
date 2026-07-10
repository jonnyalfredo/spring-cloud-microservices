package com.jonathanleite.clientapi.domain.service;

import com.jonathanleite.clientapi.api.dto.ClientPatchRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientRequestDTO;
import com.jonathanleite.clientapi.api.dto.ClientResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ClientResponseDTO create(ClientRequestDTO request);

    ClientResponseDTO update(Long id, ClientRequestDTO request);

    ClientResponseDTO patch(Long id, ClientPatchRequestDTO request);

    ClientResponseDTO activate(Long id);

    ClientResponseDTO deactivate(Long id);

    ClientResponseDTO findById(Long id);

    Page<ClientResponseDTO> findAll(String name, String email, String document, Boolean active, Pageable pageable);

    void delete(Long id);
}
