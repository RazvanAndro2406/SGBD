package com.example.proect_lab123.domain;

import java.io.Serializable;

/**
 * Auditable entity base class.
 * Extends Entity to inherit ID management.
 */
public class BaseAuditableEntity<ID> extends Entity<ID> implements Serializable {

    private static final long serialVersionUID = 7331115341259248462L;

    public BaseAuditableEntity() {
        super();
    }

    public BaseAuditableEntity(ID id) {
        super(id);
    }
}

