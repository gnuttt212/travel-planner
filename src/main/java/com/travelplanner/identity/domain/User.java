package com.travelplanner.identity.domain;

import com.travelplanner.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String displayName;
    private String avatarUrl;
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
