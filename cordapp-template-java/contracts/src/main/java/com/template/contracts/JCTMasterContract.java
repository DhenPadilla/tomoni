package com.template.contracts;

// Necessary imports:
import com.template.states.JCTMasterState;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.*;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class JCTMasterContract implements Contract {
    public SecureHash getLegalContractReference() {
        return SecureHash.Companion.sha256("link to legal contract");
    }

    public static final String ID = "com.template.contracts.JCTMasterContract";

    public static class Create implements CommandData {

    }

//    private class sortByOwningKey implements Comparator<AbstractParty>
//    {
//        // Used for sorting in ascending order of party owningKey
//        public int compare(AbstractParty a, AbstractParty b)
//        {
//            return a.hashCode() - b.hashCode();
//        }
//    }
//
//    private List<PublicKey> getOwningKeys(List<Party> parties) {
//        List<PublicKey> keys = new ArrayList<PublicKey>();
//        parties.forEach((party) -> keys.add(party.getOwningKey()));
//        return keys;
//    }


    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<JCTMasterContract.Create> command = requireSingleCommand(tx.getCommands(), JCTMasterContract.Create.class);

        // Constraints on the shape of the transaction.

        // Creating a JCT Master Contract should not have
        // any prior transaction-state input parameters
        if (!tx.getInputs().isEmpty())
            throw new IllegalArgumentException("No inputs should be consumed when issuing an JCT.");

        if (!(tx.getOutputs().size() == 1))
            throw new IllegalArgumentException("There should be one output state of type JCTState.");

        // JCT-state-specific constraints
        final JCTMasterState output = tx.outputsOfType(JCTMasterState.class).get(0);
        final Party employer = output.getEmployer();
        final Party contractor = output.getContractor();

        if(output.getProjectName().isEmpty())
            throw new IllegalArgumentException("The JCT must have a project name to be created");

        // Check if Employers == Contractors
        if(employer.equals(contractor)) {
            throw new IllegalArgumentException("The Employers must not be the same as the Contractors of the project");
        }

        // Constraints on the signers of the contract
        // Two-tiered signer groups
        final List<PublicKey> requiredSigners = command.getSigners();
        final List<PublicKey> expectedSigners = Arrays.asList(employer.getOwningKey(), contractor.getOwningKey());

        // Specify the constraints on the number of signers required
        if (requiredSigners.size() < 2)
            throw new IllegalArgumentException("There must be at least 2 signers in this transaction");
    }
}
