package com.academy.models.global_configuration;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class GlobalConfiguration extends BaseEntity {

    @Column(name="config_key", unique = true, nullable = false)
    @NotBlank
    private String configKey;

    @Lob
    @Column(name="config_value", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name="config_type", nullable = false)
    private GlobalConfigurationTypeEnum configType;

}
