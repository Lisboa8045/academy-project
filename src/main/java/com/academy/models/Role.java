package com.academy.models;

import com.academy.models.shared.BaseEntity;
import com.academy.util.FieldLengths;
import com.academy.models.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "role")
@Getter
@Setter
@ToString(callSuper = true, exclude="members")
public class Role extends BaseEntity {

    @Column(name = "name", length = FieldLengths.ROLE_MAX)
    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members;
}
