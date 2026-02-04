package com.jonathanleite.vitrine.customer.controller;

import com.jonathanleite.vitrine.customer.dto.CustomerRequestDTO;
import com.jonathanleite.vitrine.customer.dto.CustomerResponseDTO;
import com.jonathanleite.vitrine.customer.entity.CustomerEntity;
import com.jonathanleite.vitrine.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(
            @RequestBody CustomerRequestDTO request) {

        CustomerEntity customer = new CustomerEntity();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setDocument(request.getDocument());

        CustomerEntity saved = customerService.create(customer);

        CustomerResponseDTO response = new CustomerResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> findById(@PathVariable Long id) {

        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(
                        new CustomerResponseDTO(
                                customer.getId(),
                                customer.getName(),
                                customer.getEmail(),
                                customer.getActive()
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }
}
