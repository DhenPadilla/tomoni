package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.JCTContract;
import com.template.schema.PersistentJCT;
import com.template.schema.PersistentRecital;
import com.template.schema.RecitalSchemaV1;
import com.template.states.clauses.JCTRecitalState;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@BelongsToContract(JCTContract.class)
public class JCTQueryableState implements QueryableState {
    private Instant issuanceDate;
    private final UniqueIdentifier linearId;
    private final String projectName;
    private final List<Party> employer;
    private final List<Party> contractor;
    private final List<JCTRecitalState> recitals = new ArrayList<>();

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

//    public JCTQueryableState appendRecitals(List<Integer> recitalIndexes) {
//        List<JCTRecitalState> recitals = RecitalMap.getRecitalsFor(recitalIndexes);
//        JCTQueryableState withRecitals = new JCTQueryableState(projectName, this.employer, this.contractor);
//        withRecitals.recitals.addAll(recitals);
//        return withRecitals;
//    }

    public String getProjectName() { return projectName; }

    public List<Party> getEmployer() {
        return employer;
    }

    public List<Party> getContractor() {
        return contractor;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        final List<AbstractParty> parts = new ArrayList<>();
        parts.addAll(employer);
        parts.addAll(contractor);
        return parts;
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }


    // JPA Methods

    /* Generate the persistent representation of this state. */
    @NotNull
    @Override
    public PersistentJCT generateMappedObject(@NotNull MappedSchema schema) {

        // Create list of PersistentRecital entities against every Recital object.
        List<PersistentRecital> persistentRecitals = new ArrayList<>();
        if(recitals != null && recitals.size() > 0) {
            for(JCTRecitalState recital: recitals){
                PersistentRecital persistentClaim = new PersistentRecital(
                        recital.getRecitalDesc(),
                        recital.getRecitalStatus(),
                        recital.getIssuanceDate()
                );
                persistentRecitals.add(persistentClaim);
            }
        }
        if(schema instanceof RecitalSchemaV1) {
            return new PersistentJCT(
                    this.projectName,
                    this.employer,
                    this.contractor,
                    persistentRecitals,
                    this.issuanceDate
                    );
        }
        else {
            throw new IllegalArgumentException("Unsupported Schema");
        }
    }

    /* Return a list of schema supported by this state */
    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new RecitalSchemaV1());
    }

}
