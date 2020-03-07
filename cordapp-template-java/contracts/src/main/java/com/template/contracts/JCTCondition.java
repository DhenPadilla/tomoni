package com.template.contracts;

// Add these imports:

import com.template.states.IOUState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// ************
// * Contract *
// ************
public abstract class JCTCondition implements Contract {
    // This is used to identify our contract when building a transaction.

    // Create command used to create the contract
    public interface Commands extends CommandData {
        class Create extends TypeOnlyCommandData implements Commands {}
        class VoteOnCondition extends TypeOnlyCommandData implements Commands {}
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
        if (command instanceof Commands.VoteOnCondition) {
            voteOnCondition(tx);
        }
    }

    protected abstract void verifyCreate(LedgerTransaction tx);

    protected abstract void voteOnCondition(LedgerTransaction tx);
}