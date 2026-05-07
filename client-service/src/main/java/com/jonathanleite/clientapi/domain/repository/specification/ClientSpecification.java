package com.jonathanleite.clientapi.domain.repository.specification;

import com.jonathanleite.clientapi.domain.entity.Client;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                email == null ? null :
                        criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<Client> hasDocument(String document) {
        return (root, query, criteriaBuilder) ->
                document == null ? null :
                        criteriaBuilder.equal(root.get("document"), document);
    }
}
