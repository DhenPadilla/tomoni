package com.template.states;

import com.template.contracts.JCTMasterContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.*;
import static java.util.Collections.*;

// *********
// * State *
// *********
@BelongsToContract(JCTMasterContract.class)
public class JCTMasterState implements ContractState {

    private final Party employer;
    private final Party contractor;
    private final String projectName;

    public JCTMasterState(String projectName, Party employer, Party contractor) {
        this.projectName = projectName;
        this.employer = employer;
        this.contractor = contractor;
    }

    public String getProjectName() {
        return projectName;
    }

    public Party getEmployer() {
        return employer;
    }

    public Party getContractor() {
        return contractor;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(employer, contractor);
    }
}