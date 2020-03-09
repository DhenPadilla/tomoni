package com.template.schema;

import net.corda.core.identity.Party;
import net.corda.core.schemas.PersistentState;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;


/**
 * JPA Entity for saving claim details to the database table
 */
@Entity
@Table(name = "JCT_DETAIL")
public class PersistentJCT extends PersistentState implements Serializable {

    @Column private final String projectName;
    @Column private final List<Party> employer;
    @Column private final List<Party> contractor;
    @Column private final Instant issuanceDate;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "output_index", referencedColumnName = "output_index"),
            @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
    })
    private List<PersistentRecital> recitals;


    public PersistentJCT(Instant issuanceDate) {
        this.issuanceDate = issuanceDate;
        this.projectName = null;
        this.employer = null;
        this.contractor = null;
        this.recitals = null;
    }

    public PersistentJCT(String projectName, List<Party> employer, List<Party> contractor,
                         List<PersistentRecital> recitals, Instant issuanceDate) {
        this.issuanceDate = issuanceDate;
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
        this.recitals = recitals;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<Party> getEmployer() {
        return employer;
    }

    public List<Party> getContractor() {
        return contractor;
    }

    public Instant getIssuanceDate() {
        return issuanceDate;
    }

    public List<PersistentRecital> getRecitals() {
        return recitals;
    }
}
