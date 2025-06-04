package com.academy.models.global_configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class GlobalConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="configKey")
    private String configKey;

    @Column(name="configValue")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name="configType")
    private GlobalConfigurationType configType;

    // Getters and Setters
}
