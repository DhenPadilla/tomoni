package com.template.contracts;

// Add these imports:

import com.template.states.ReportState;
import com.template.states.ReportStatus;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ReportContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.ReportContract";

    public interface Commands extends CommandData {
        class AddReportDocument extends TypeOnlyCommandData implements Commands {}
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        // Tests whether there exists a *single* 'AddReportDocument'
        // command is present within the transaction/contract
        List<CommandWithParties<CommandData>> commands = tx.getCommands();
        // Check if inputted command is of type: "ScheduleEscrowContract.Command"
        // and if it is, dismiss from this verify.
        // Verification is handled in ScheduleEscrowContract
        if(commands.stream().anyMatch(com -> com.getValue() instanceof ScheduleEscrowContract.Commands)) {
            return;
        }
        final ReportContract.Commands command = tx.findCommand(ReportContract.Commands.class, cmd -> true).getValue();
        if(command instanceof ReportContract.Commands.AddReportDocument) {
            requireThat(require -> {
                require.using("One output state is a type of: 'ReportState'",
                        tx.getOutputStates().get(0) instanceof ReportState);
                require.using("No input state must be consumed when creating a report.",
                        tx.getInputs().isEmpty());
                require.using("Single report state must be output in transaction.",
                        tx.getOutputs().size() == 1);
                ReportState outputState = (ReportState) tx.getOutputStates().get(0);
                require.using("Report must not have empty inputs",
                        !outputState.checkIfEmpty());
                require.using("Output ReportState must have status: UNSEEN",
                        outputState.getStatus() == ReportStatus.ISSUED);

                return null;
            });
        }

    }
}