package com.bci.repository;

import com.bci.entity.Phone;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;


public interface PhoneRepository extends ReactiveCrudRepository<Phone, Long> {
    Flux<Phone> findByUserId(UUID userId);
}

