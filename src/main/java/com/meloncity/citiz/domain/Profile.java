package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profile", indexes = {
        @Index(name = "ix_profile_email", columnList = "email", unique = true)
})
@SequenceGenerator(name = "profile_seq", sequenceName = "profile_seq", allocationSize = 1)
@Getter
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_seq")
    private Long id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Post> posts = new ArrayList<>();

    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPassword(String password) { this.password = password; }
}
