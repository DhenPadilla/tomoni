package com.template.schema;

import com.template.states.clauses.JCTRecitalState;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;


/**
 * JPA Entity for saving claim details to the database table
 */
@Entity
@Table(name = "CLAIM_DETAIL")
public class PersistentRecital {

    @Id private final UUID id;
    @Column private final String recitalDesc;
    @Column private final String recitalStatus;
    @Column private final Instant issuanceDate;

    /**
     * Default constructor required by Hibernate
     */
    public PersistentRecital() {
        this.id = null;
        this.recitalDesc = null;
        this.recitalStatus = null;
        this.issuanceDate = null;
    }

    public PersistentRecital(String recitalDesc, String recitalStatus, Instant issuanceDate) {
        this.id = UUID.randomUUID();
        this.recitalDesc = recitalDesc;
        this.recitalStatus = recitalStatus;
        this.issuanceDate = issuanceDate;
    }

    // JCT-based state
    public PersistentRecital copy() {
        return new PersistentRecital(this.recitalDesc, this.recitalStatus, this.issuanceDate);
    }

    public PersistentRecital signDate(Instant issuanceDate) {
        PersistentRecital stateWithSignedDate = new PersistentRecital(this.recitalDesc, this.recitalStatus, issuanceDate);
        return stateWithSignedDate;
    }

    public UUID getId() {
        return id;
    }

    public String getRecitalDesc() {
        return recitalDesc;
    }

    public String getRecitalStatus() {
        return recitalStatus;
    }

    public Instant getIssuanceDate() {
        return issuanceDate;
    }
}
