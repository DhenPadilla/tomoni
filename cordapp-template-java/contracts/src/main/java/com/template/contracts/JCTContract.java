package com.template.contracts;

// Add these imports:

import com.template.states.JCTState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class JCTContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.JCTContract";

    // Create command used to create the contract
    public interface Commands extends CommandData {
        class Create extends TypeOnlyCommandData implements Commands {}
        class AddRecitals extends TypeOnlyCommandData implements Commands {}
        class IssueEscrowAgreement extends TypeOnlyCommandData implements Commands {}
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
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        // Tests whether there exists a *single* 'Create'
        // command is present within the transaction/contract

        final Commands command = tx.findCommand(Commands.class, cmd -> true).getValue();

        // CREATE THE JCT
        if (command instanceof Commands.Create) {
            verifyCreate(tx);
        }

        // ADD RECITALS TO JCT
        if (command instanceof Commands.AddRecitals) {
            verifyRecitals(tx);
        }
    }

    private void verifyCreate(LedgerTransaction tx) {
        // If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.
        final CommandWithParties<Commands.Create> create = requireSingleCommand(tx.getCommands(), Commands.Create.class);

        requireThat(require -> {
            // Input/Output state requirements.
            require.using("No inputs should be consumed when issuing an JCT.", tx.getInputs().isEmpty());
            require.using("There should be one output state of type JCTState.", tx.getOutputs().size() == 1);

            // State-specific requirements
            final JCTState output = tx.outputsOfType(JCTState.class).get(0);
            final List<Party> employer = output.getEmployer();
            final List<Party> contractor = output.getContractor();
            require.using("A Project Name must be given to create a JCT", !output.getProjectName().isEmpty());
            require.using("The Contractor cannot be the Employer", !employer.equals(contractor));

            // Constraints on the signers of the contract
            final List<PublicKey> requiredSigners = create.getSigners();
            final List<PublicKey> expectedSigners = new ArrayList<>();
            expectedSigners.addAll(getOwningKeys(employer));
            expectedSigners.addAll(getOwningKeys(contractor));
            require.using("There must be 2 or more signers in this transaction", expectedSigners.size() > 1);
            require.using("There must be 2 or more signers in this transaction", expectedSigners.size() > 1);
            require.using("The Employer and Contractor must be signers to this transaction", expectedSigners.containsAll(expectedSigners));

            return null;
        });
    }

    private void verifyRecitals(LedgerTransaction tx) {
        final CommandWithParties<Commands.AddRecitals> addRecitals = requireSingleCommand(tx.getCommands(), Commands.AddRecitals.class);

        // Adding recitals to a JCT must consume a JCT state
        requireThat(require -> {
            // Input/Output state requirements.
            require.using("No inputs should be consumed when issuing an JCT.", tx.getInputStates().size() == 1);
            require.using("There should be one output state of type JCTState.", tx.getOutputs().size() == 1);

            // State-specific requirements
            final JCTState input = tx.inputsOfType(JCTState.class).get(0); // Must only be one in the list
            final List<Party> prevParticipants = input.getEmployer();
            prevParticipants.addAll(input.getContractor());

            final JCTState output = tx.outputsOfType(JCTState.class).get(0);
            final List<Party> newParticipants = output.getEmployer();
            newParticipants.addAll(output.getContractor());
            require.using("The new JCTState must have the same participating parties",
                    prevParticipants.containsAll(newParticipants));

            // Constraints on the signers of the contract -- None so far?
            return null;
        });
    }
}