package com.example.shoppinglist.resource.persistance.audit;

import com.example.shoppinglist.resource.context.TenantContext;
import com.example.shoppinglist.resource.persistance.listener.LifecycleListener;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Getter
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class, LifecycleListener.class})
public abstract class Auditable {

    @Column(name = "version")
    @Version
    private Long version;

    @Column(name = "createdOn", nullable = false, updatable = false)
    @CreatedDate
    private Long createdOn;

    @Column(name = "updatedOn", nullable = false)
    @LastModifiedDate
    private Long updatedOn;

    @Column(name = "tenantId", nullable = false, updatable = false, length = 36)
    private String tenantId;

    @PrePersist
    private void setTenantId() {
        this.tenantId = TenantContext.getTenantId();
    }

}