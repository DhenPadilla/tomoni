package com.template.contracts;

// Add these imports:
import com.template.states.IOUState;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * Contract *
// ************
public class IOUContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.IOUContract";

    // Create command used to create the contract
    public static class Create implements CommandData {

    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        // Tests whether there exists a *single* 'Create'
        // command is present within the transaction/contract
        final CommandWithParties<IOUContract.Create> command = requireSingleCommand(tx.getCommands(), IOUContract.Create.class);

        //If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.

        // Constraints on the shape of the transaction.
        if (!tx.getInputs().isEmpty())
            throw new IllegalArgumentException("No inputs should be consumed when issuing an IOU.");
        if (!(tx.getOutputs().size() == 1))
            throw new IllegalArgumentException("There should be one output state of type IOUState.");

        // IOU-state-specific constraints
        final IOUState output = tx.outputsOfType(IOUState.class).get(0);
        final Party lender = output.getEmployer();
        final Party borrower = output.getContractor();

        if(output.getValue() <= 0)
            throw new IllegalArgumentException("The IOU's value must be non-negative to be created");
        if(lender.equals(borrower))
            throw new IllegalArgumentException("The IOU's lender-party must be different from the borrower-party");

        // Constraints on the signers of the contract
        // Two-tiered signer groups
        final List<PublicKey> requiredSigners = command.getSigners();
        final List<PublicKey> expectedSigners = Arrays.asList(borrower.getOwningKey(), lender.getOwningKey());

        // Specify the constraints on the number of signers required
        if (requiredSigners.size() != 2)
            throw new IllegalArgumentException("There must be 2 signers in this transaction");
        if (!(expectedSigners.containsAll(expectedSigners)))
            throw new IllegalArgumentException("The borrower and lender must be signers to this transaction");
    }
}