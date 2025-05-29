package com.academy.models;

import com.academy.models.service.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tag")
@Getter
@Setter
@ToString
public class Tag {

    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotBlank
    @Column(name="name", unique = true)
    private String name;

    @NotNull
    @Column(name="is_custom")
    private Boolean isCustom;

    @Column(name="created_at", updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Service> services = new ArrayList<>();

    public void removeAllServices() {
        for (Service service : new ArrayList<>(services)) {
            service.getTags().remove(this);
        }
        services.clear();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isCustom=" + isCustom +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
