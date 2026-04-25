package org.example.backend.modules.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "users", schema = "public")
public class User extends BaseEntity {

    @Column(name = "fullname",length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone",length = 20, unique = true, nullable = false)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "ekyc_status", nullable = false)
    private String ekycStatus;

    @Column(name = "avatar_url")
    private String avatarUrl;

    //Add and remove wallet here
}
