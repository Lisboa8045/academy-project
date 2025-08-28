package com.academy.models.member;

import com.academy.models.Role;
import com.academy.models.appointment.Appointment;
import com.academy.models.availability.MemberAvailability;
import com.academy.models.service.Service;
import com.academy.models.shared.BaseEntity;
import com.academy.utils.FieldLengths;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString(callSuper = true, exclude = {"memberAvailabilities", "appointments", "createdServices", "role"})
public class Member extends BaseEntity {

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAvailability> memberAvailabilities = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Appointment> appointments = new ArrayList<>();

    @Column(name = "username", unique = true, nullable = false, length = FieldLengths.USERNAME_MAX)
    private String username;

    @Column(name  = "password", nullable = false) // don't use password_max here, the stored password can be bigger due to hashing/salting
    private String password;

    @Column(name = "email", nullable = false, length = FieldLengths.EMAIL_MAX)
    private String email;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatusEnum status;

    @Column(name = "address", length = FieldLengths.ADDRESS_MAX)
    private String address;

    @Column(name = "postal_code", length = FieldLengths.POSTAL_CODE_MAX)
    private String postalCode;

    @Column(name = "phone_number", length = FieldLengths.PHONE_NUMBER_MAX)
    private String phoneNumber;

    @OneToMany(mappedBy = "owner")
    private List<Service> createdServices;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "deletion_tokens_sent_today")
    private Integer deletionTokensSentToday;
}
