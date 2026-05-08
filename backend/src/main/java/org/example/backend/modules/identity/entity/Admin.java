package org.example.backend.modules.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "admin", schema = "identity")
public class Admin extends BaseEntity {
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Account account;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "department")
    private String department;

    @Column(name = "internal_note")
    private String internalNote;
}
