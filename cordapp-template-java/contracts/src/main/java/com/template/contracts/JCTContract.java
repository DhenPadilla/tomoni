package com.template.contracts;

// Add these imports:

import com.template.states.JCTState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// ************
// * Contract *
// ************
public class JCTContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.JCTContract";

    // Create command used to create the contract
    public static class Create implements CommandData {

    }

    private class sortByOwningKey implements Comparator<AbstractParty> {
        // Used for sorting in ascending order of party owningKey
        public int compare(AbstractParty a, AbstractParty b)
        {
            return a.hashCode() - b.hashCode();
        }
    }

    private List<PublicKey> getOwningKeys(List<Party> parties) {
        List<PublicKey> keys = new ArrayList<PublicKey>();
        parties.forEach((party) -> keys.add(party.getOwningKey()));
        return keys;
    }


    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        // Tests whether there exists a *single* 'Create'
        // command is present within the transaction/contract
        final CommandWithParties<JCTContract.Create> command = requireSingleCommand(tx.getCommands(), JCTContract.Create.class);

        //If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.

        // Constraints on the shape of the transaction.
        if (!tx.getInputs().isEmpty())
            throw new IllegalArgumentException("No inputs should be consumed when issuing an IOU.");
        if (!(tx.getOutputs().size() == 1))
            throw new IllegalArgumentException("There should be one output state of type IOUState.");

        // JCT-state-specific constraints
        final JCTState output = tx.outputsOfType(JCTState.class).get(0);
        final List<Party> employer = output.getEmployer();
        final List<Party> contractor = output.getContractor();

        if(output.getProjectName().isEmpty())
            throw new IllegalArgumentException("A Project Name must be given to create a JCT");
        if(employer.equals(contractor))
            throw new IllegalArgumentException("The Contractor cannot be Employer");

        // Constraints on the signers of the contract
        // Two-tiered signer groups
        final List<PublicKey> requiredSigners = command.getSigners();
        final List<PublicKey> expectedSigners = new ArrayList<>();
        expectedSigners.addAll(getOwningKeys(employer));
        expectedSigners.addAll(getOwningKeys(contractor));

        // Specify the constraints on the number of signers required
        if (requiredSigners.size() <= 2)
            throw new IllegalArgumentException("There must be more than signers in this transaction");
        if (!(expectedSigners.containsAll(expectedSigners)))
            throw new IllegalArgumentException("The Employer and Contractor must be signers to this transaction");
    }
}