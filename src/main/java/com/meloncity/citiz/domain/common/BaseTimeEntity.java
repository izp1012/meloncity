// src/main/java/com/example/domain/common/BaseTimeEntity.java
package com.meloncity.citiz.domain.common;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "create_date", updatable = false, nullable = false)
    protected LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "update_date", nullable = false)
    protected LocalDateTime updateDate;

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createDate = now;
        this.updateDate = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
