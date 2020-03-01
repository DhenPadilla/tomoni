package com.template.states;

import com.template.contracts.JCTContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(JCTContract.class)
public class JCTState implements ContractState {

    private final String projectName;
    private final List<Party> employer;
    private final List<Party> contractor;

    public JCTState(String projectName, List<Party> employer, List<Party> contractor) {
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

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
        return parts;a
    }
}