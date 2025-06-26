package com.academy.models.service;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="service_images")
@Getter
@Setter
public class ServiceImages extends BaseEntity {


    @Column(name="image")
    private String image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "service_id")
    private Service service;
}
