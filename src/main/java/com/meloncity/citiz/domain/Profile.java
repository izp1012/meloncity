package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false) private String email;
    @Column(nullable = false, length = 20) private String name;
    @Column(nullable = false) private String password;
    private String imageUrl;

    @Builder
    public Profile (String email, String name, String password, String imageUrl) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.imageUrl = imageUrl;
    }

}
