package com.template.states;

import com.template.contracts.JCTContract;
import com.template.contracts.Recital;
import com.template.contracts.RecitalMap;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// *********
// * State *
// *********
@BelongsToContract(JCTContract.class)
public class JCTState implements ContractState, LinearState {

    private Instant issuanceDate;
    private final UniqueIdentifier linearId;
    private final String projectName;
    private final List<Party> employer;
    private final List<Party> contractor;
    private final List<Recital> recitals = new ArrayList<>();

    @ConstructorForDeserialization
    public JCTState(UniqueIdentifier linearId, String projectName, List<Party> employer, List<Party> contractor) {
        this.linearId = linearId;
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

    public JCTState(String projectName, List<Party> employer, List<Party> contractor) {
        this.linearId = new UniqueIdentifier();
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

    // JCT-based state
    public JCTState copy() {
        return new JCTState(this.projectName, this.employer, this.contractor);
    }

    public JCTState signDate(Instant issuanceDate) {
        JCTState stateWithSignedDate = new JCTState(this.projectName, this.employer, this.contractor);
        stateWithSignedDate.issuanceDate = issuanceDate;
        return stateWithSignedDate;
    }

    public JCTState appendRecitals(List<Integer> recitalIndexes) {
        List<Recital> recitals = RecitalMap.getRecitalsFor(recitalIndexes);
        JCTState withRecitals = new JCTState(projectName, this.employer, this.contractor);
        withRecitals.recitals.addAll(recitals);
        return withRecitals;
    }

//    @NotNull
//    @Override
//    public CommandAndState withNewOwner(@NotNull AbstractParty newOwner) {
//        return new CommandAndState(new .Commands.Move(), new State(this.issuance, newOwner, this.faceValue, this.maturityDate));
//    }

    public String getProjectName() { return projectName; }

    public List<Party> getEmployer() {
        return employer;
    }

    public List<Party> getContractor() {
        return contractor;
    }

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

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return null;
    }
}