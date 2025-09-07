// src/main/java/com/example/domain/common/BaseTimeEntity.java
package com.meloncity.citiz.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "create_date", updatable = false, nullable = false)
    protected LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "update_date", nullable = false)
    protected LocalDateTime updateDate;

    public LocalDateTime getCreateDate() { return createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }
}
