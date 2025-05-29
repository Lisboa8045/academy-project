package com.academy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Availability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Appointment> appointments = new ArrayList<>();

    @Column(name = "username", unique = true)
    private String username;

    @Column(name  = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}
