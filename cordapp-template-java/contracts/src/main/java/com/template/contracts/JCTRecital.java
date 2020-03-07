package com.template.contracts;

// Add these imports:

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * Contract *
// ************
public abstract class JCTRecital implements Contract {
    // This is used to identify our contract when building a transaction.

    // Create command used to create the contract
    public interface Commands extends CommandData {
        class Create extends TypeOnlyCommandData implements Commands {}
        class AppendRecitals extends TypeOnlyCommandData implements Commands {}
    }


    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        // Check whether a command is entered into the transaction,
        // If true, get it's value (used to run the respected command
        final Commands command = tx.findCommand(Commands.class, cmd -> true).getValue();

        //If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.

        // Create the JCTCondition
        if (command instanceof Commands.Create) {
            verifyCreate(tx);
        }

        // Vote on the JCTCondition
        if (command instanceof Commands.AppendRecitals) {
            voteOnCondition(tx);
        }
    }

    protected abstract void verifyCreate(LedgerTransaction tx);

    protected abstract void voteOnCondition(LedgerTransaction tx);
}