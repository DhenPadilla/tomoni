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

    private final List<Party> employers = emptyList();
    private final List<Party> contractors = emptyList();
    private final String projectName;

    public JCTMasterState(String projectName, List<Party> employers, List<Party> contractors) {
        this.projectName = projectName;
        this.employers.addAll(employers);
        this.contractors.addAll(contractors);
    }

    public String getProjectName() {
        return projectName;
    }

    public List<Party> getEmployers() {
        return employers;
    }

    public List<Party> getContractors() {
        return contractors;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> allParts = new ArrayList<>();
        allParts.addAll(employers);
        allParts.addAll(contractors);
        return allParts;
    }
}