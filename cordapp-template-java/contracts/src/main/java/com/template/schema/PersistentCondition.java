package com.template.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;


/**
 * JPA Entity for saving claim details to the database table
 */
@Entity
@Table(name = "CLAIM_DETAIL")
public class PersistentCondition {

    @Id private final UUID id;
    @Column private final String claimNumber;
    @Column private final String claimDescription;
    @Column private final Integer claimAmount;

    /**
     * Default constructor required by Hibernate
     */
    public PersistentCondition() {
        this.id = null;
        this.claimNumber = null;
        this.claimDescription = null;
        this.claimAmount = null;
    }

    public PersistentCondition(String claimNumber, String claimDescription, Integer claimAmount) {
        this.id = UUID.randomUUID();
        this.claimNumber = claimNumber;
        this.claimDescription = claimDescription;
        this.claimAmount = claimAmount;
    }

    public UUID getId() {
        return id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public String getClaimDescription() {
        return claimDescription;
    }

    public Integer getClaimAmount() {
        return claimAmount;
    }
}
