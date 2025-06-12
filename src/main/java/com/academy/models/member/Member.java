package com.academy.models.member;

import com.academy.models.Appointment;
import com.academy.models.Availability;
import com.academy.models.Role;
import com.academy.models.service.Service;
import com.academy.util.FieldLengths;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


@Entity
@Table(name = "member")
@Getter
@Setter
@ToString(exclude = {"availabilities", "appointments", "createdServices", "role"})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Appointment> appointments = new ArrayList<>();

    @Column(name = "username", unique = true, nullable = false, length = FieldLengths.USERNAME_MAX)
    private String username;

    @Column(name  = "password", nullable = false) // don't use password_max here, the stored password can be bigger due to hashing/salting
    private String password;

    @Column(name = "email", nullable = false, length = FieldLengths.EMAIL_MAX)
    private String email;

    @Column(name = "enabled") //TODO should be nullable quando alterarem os testes para usar service
    private boolean enabled;

    @Column(name = "status") //TODO should be nullable quando alterarem os testes para usar service
    @Enumerated(EnumType.STRING)
    private MemberStatusEnum status;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "address", length = FieldLengths.ADDRESS_MAX)
    private String address;

    @Column(name = "postal_code", length = FieldLengths.POSTAL_CODE_MAX)
    private String postalCode;

    @Column(name = "phone_number", length = FieldLengths.PHONE_NUMBER_MAX)
    private String phoneNumber;

    @OneToMany(mappedBy = "owner")
    private List<Service> createdServices;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "profile-picture")
    private String profilePicture;


}
