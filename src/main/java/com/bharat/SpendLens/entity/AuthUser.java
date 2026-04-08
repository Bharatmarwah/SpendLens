package com.bharat.SpendLens.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auth_user")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean profileCompleted;


}




