package com.jonathanleite.vitrine.customer.service;

import com.jonathanleite.vitrine.customer.entity.CustomerEntity;

import java.util.Optional;

public interface CustomerService {

    CustomerEntity create(CustomerEntity customer);

    Optional<CustomerEntity> findById(Long id);
}
