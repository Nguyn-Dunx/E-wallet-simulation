package org.example.backend.modules.identity.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.modules.identity.common.enums.RoleName;

@Entity
@Table(name = "role", schema = "identity")
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private RoleName name;
}
