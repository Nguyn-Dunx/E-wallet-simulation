package org.example.backend.modules.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.BaseEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(name = "users", schema = "public")
public class User extends BaseEntity {

    @Column(name = "fullname",length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone",length = 20, unique = true, nullable = false)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    //Add and remove wallet here
}
