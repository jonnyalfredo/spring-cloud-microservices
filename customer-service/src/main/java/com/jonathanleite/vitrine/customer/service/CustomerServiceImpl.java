package com.jonathanleite.vitrine.customer.service;

import com.jonathanleite.vitrine.customer.entity.CustomerEntity;
import com.jonathanleite.vitrine.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerEntity create(CustomerEntity customer) {

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        return customerRepository.save(customer);
    }

    @Override
    public Optional<CustomerEntity> findById(Long id) {
        return customerRepository.findById(id);
    }
}
