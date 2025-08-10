package com.bci.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table("phones")
public class Phone {
    @Id
    private Long id;

    @Column
    private String number;

    @Column
    private Integer citycode;

    @Column("country_code")
    private String countrycode;

    @Column("user_id")
    private UUID userId;
}
