package com.ing.controller;

import com.ing.model.Customer;
import com.ing.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final CustomerRepository repository;

    @PostMapping("/customer")
    public Customer createCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @GetMapping("/customer/{id}")
    public Customer getCustomerById(@PathVariable("id") Long id) {
        return repository.findById(id).orElseThrow(RuntimeException::new);
    }
}
