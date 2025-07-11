package com.academy.models;

import com.academy.models.service.Service;
import com.academy.models.shared.BaseEntity;
import com.academy.utils.FieldLengths;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tag")
@Getter
@Setter
@ToString(callSuper = true, exclude="services")
public class Tag extends BaseEntity {

    @NotBlank
    @Column(name="name", unique = true, length = FieldLengths.TAG_NAME_MAX)
    private String name;

    @Column(name="custom", nullable = false)
    private Boolean custom;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Service> services = new ArrayList<>();

}