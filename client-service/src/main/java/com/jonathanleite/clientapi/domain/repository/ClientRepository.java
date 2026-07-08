package com.jonathanleite.clientapi.domain.repository;

import com.jonathanleite.clientapi.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends
        JpaRepository<Client, Long>,
        JpaSpecificationExecutor<Client> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByDocument(String document);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);
}
