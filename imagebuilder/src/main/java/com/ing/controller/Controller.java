package com.ing.controller;

import com.ing.model.Customer;
import com.ing.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Controller {

    private final CustomerRepository repository;

    @PostMapping("/customer")
    public Customer createCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = repository.save(customer);
        log.info("The following customer was successfully created: {}", createdCustomer);
        return createdCustomer;
    }

    @GetMapping("/customer/{id}")
    public Customer getCustomerById(@PathVariable("id") Long id) {
        return repository.findById(id).orElseThrow(RuntimeException::new);
    }
}
