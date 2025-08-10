package com.bci.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Table("users")
public class User {
    @Id
    private UUID id;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @Transient
    private List<Phone> phones = new ArrayList<>();

    @Column
    private LocalDateTime created;

    @Column
    private LocalDateTime lastLogin;

    @Column
    private String token;

    @Column("is_active")
    private Boolean isActive;




}
