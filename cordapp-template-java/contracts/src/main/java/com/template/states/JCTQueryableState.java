package com.template.states;

import com.template.contracts.JCTContract;
import com.template.contracts.JCTRecital;
import com.template.contracts.RecitalMap;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@BelongsToContract(JCTContract.class)
public class JCTQueryableState implements QueryableState {
    private Instant issuanceDate;
    private final UniqueIdentifier linearId;
    private final String projectName;
    private final List<Party> employer;
    private final List<Party> contractor;
    private final List<JCTRecital> recitals = new ArrayList<>();

    @ConstructorForDeserialization
    public JCTQueryableState(UniqueIdentifier linearId, String projectName, List<Party> employer, List<Party> contractor) {
        this.linearId = linearId;
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

    public JCTQueryableState(String projectName, List<Party> employer, List<Party> contractor) {
        this.linearId = new UniqueIdentifier();
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

    // JCT-based state
    public JCTQueryableState copy() {
        return new JCTQueryableState(this.projectName, this.employer, this.contractor);
    }

    public JCTQueryableState signDate(Instant issuanceDate) {
        JCTQueryableState stateWithSignedDate = new JCTQueryableState(this.projectName, this.employer, this.contractor);
        stateWithSignedDate.issuanceDate = issuanceDate;
        return stateWithSignedDate;
    }

    public JCTQueryableState appendRecitals(List<Integer> recitalIndexes) {
        List<JCTRecital> recitals = RecitalMap.getRecitalsFor(recitalIndexes);
        JCTQueryableState withRecitals = new JCTQueryableState(projectName, this.employer, this.contractor);
        withRecitals.recitals.addAll(recitals);
        return withRecitals;
    }

    public String getProjectName() { return projectName; }

    public List<Party> getEmployer() {
        return employer;
    }

    public List<Party> getContractor() {
        return contractor;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        return null;
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return null;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        final List<AbstractParty> parts = new ArrayList<>();
        parts.addAll(employer);
        parts.addAll(contractor);
        return parts;
    }
}
