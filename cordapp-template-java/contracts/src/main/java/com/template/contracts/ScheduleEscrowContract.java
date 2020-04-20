package com.template.contracts;

// Add these imports:

import com.template.states.JCTJob;
import com.template.states.JCTScheduleEscrowState;
import com.template.states.JCTJobStatus;
import com.template.states.ScheduleEscrowState;
import net.corda.core.contracts.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ScheduleEscrowContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.ScheduleEscrowContract";

    public interface Commands extends CommandData {
        class CreateSchedule extends TypeOnlyCommandData implements Commands {}
        // `milestoneIndex` is the index of the milestone being updated in the list of milestones.
        class StartJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public StartJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class FinishJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public FinishJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class RejectJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public RejectJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class AcceptJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public AcceptJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class SendPayment extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public SendPayment(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        // Tests whether there exists a *single* 'Create'
        // command is present within the transaction/contract

        final Commands command = tx.findCommand(Commands.class, cmd -> true).getValue();

        // CREATE THE ESCROW SCHEDULE CONTRACT
        if (command instanceof Commands.CreateSchedule) {
            verifyCreate(tx);
        }

        // ADD RECITALS TO JCT
        if (command instanceof Commands.StartJob) {
            startJobWithIndex(tx);
        }
    }

    private List<PublicKey> getOwningKeys(List<Party> parties) {
        List<PublicKey> keys = new ArrayList<PublicKey>();
        parties.forEach((party) -> keys.add(party.getOwningKey()));
        return keys;
    }

    private void verifyCreate(LedgerTransaction tx) {
        // If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.
        final CommandWithParties<Commands.CreateSchedule> command = requireSingleCommand(tx.getCommands(), Commands.CreateSchedule.class);

        requireThat(require -> {
            // Input/Output state requirements.
            require.using("No inputs should be consumed when issuing a Schedule.", tx.getInputs().isEmpty());
            require.using("There should be one output state of type Schedule Escrow.", tx.getOutputs().size() == 1);

            // State-specific requirements
            final ScheduleEscrowState jobOutput = tx.outputsOfType(ScheduleEscrowState.class).get(0);
            final List<Party> employers = jobOutput.getEmployers();
            final List<Party> contractors = jobOutput.getContractors();
            final List<PublicKey> expectedSigners = new ArrayList<>();

            require.using("The developer and the contractor should be different parties.", !employers.containsAll(contractors));
            require.using("All the jobs should be unstarted/pending.", jobOutput.getJobs().stream().anyMatch(jctJob -> jctJob.getStatus() != JCTJobStatus.PENDING));

            expectedSigners.addAll(getOwningKeys(employers));
            expectedSigners.addAll(getOwningKeys(contractors));
            require.using("The employers and contractors should be required signers.",
                    command.getSigners().containsAll(expectedSigners));

            require.using("Contract Amount must be greater zero", jobOutput.getContractSum()> 0.0);

            return null;
        });
    }

    private void startJobWithIndex(LedgerTransaction tx) {
        final CommandWithParties<Commands.StartJob> command = requireSingleCommand(tx.getCommands(), Commands.StartJob.class);
        requireThat(require -> {

            require.using("One JobState input should be consumed.", tx.getInputs().size() == 1);
            require.using("One JobState output should be produced.", tx.getOutputs().size() == 1);

            ScheduleEscrowState jobInputs =  tx.inputsOfType(ScheduleEscrowState.class).get(0);
            ScheduleEscrowState jobOutputs =  tx.outputsOfType(ScheduleEscrowState.class).get(0);
            Integer jobIndex = new Commands.StartJob(command.getValue().jobIx).jobIx;
            JCTJob inputModifiedJob = jobInputs.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutputs.getJobs().get(jobIndex);

            // Signers:
            final List<Party> employers = jobOutputs.getEmployers();
            final List<Party> contractors = jobOutputs.getContractors();
            final List<PublicKey> expectedSigners = new ArrayList<>();

            expectedSigners.addAll(getOwningKeys(employers));
            expectedSigners.addAll(getOwningKeys(contractors));

            require.using("The modified milestone should have an input status of PENDING.",
                    inputModifiedJob.getStatus() == JCTJobStatus.PENDING);
            require.using("The Job should have an output status of IN_PROGRESS.",
                    outputModifiedJob.getStatus() == JCTJobStatus.IN_PROGRESS);
            require.using("The modified Job's description and amount shouldn't change.",
                    inputModifiedJob.copy().status(JCTJobStatus.IN_PROGRESS).build() == outputModifiedJob);

            List<JCTJob> otherInputMilestones = jobInputs.getJobs();
            otherInputMilestones.remove(inputModifiedJob);
            List<JCTJob> otherOutputMilestones = jobOutputs.getJobs();
            otherOutputMilestones.remove(outputModifiedJob);

            require.using("All other jobs mustn't be changed", otherInputMilestones == otherOutputMilestones);
            require.using("All employers and contractors must be required signers.", command.getSigners().containsAll(expectedSigners));

            return null;
        });
    }


}