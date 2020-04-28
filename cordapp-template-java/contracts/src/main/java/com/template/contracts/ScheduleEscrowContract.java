package com.template.contracts;

// Add these imports:

import com.template.states.*;
import net.corda.core.contracts.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
        class StartJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public StartJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class DeclareJobComplete extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public DeclareJobComplete(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class ConfirmJobComplete extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public ConfirmJobComplete(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class ContinueJob extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public ContinueJob(Integer jobIx) {
                this.jobIx = jobIx;
            }
        }
        class RequestAmountModification extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            private Double amount;
            public RequestAmountModification(Integer jobIx, Double amount) {
                this.jobIx = jobIx;
                this.amount = amount;
            }
        }
        class AcceptAmountModification extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            public AcceptAmountModification(Integer jobIx, Double amount) {
                this.jobIx = jobIx;
            }
        }
        class RequestExpectedDateModification extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            private LocalDate delayToDate;
            public RequestExpectedDateModification(Integer jobIx, LocalDate delayToDate) {
                this.jobIx = jobIx;
                this.delayToDate = delayToDate;
            }
        }
        class AcceptExpectedDateModification extends TypeOnlyCommandData implements Commands {
            private Integer jobIx;
            private LocalDate delayToDate;
            public AcceptExpectedDateModification(Integer jobIx, LocalDate delayToDate) {
                this.jobIx = jobIx;
                this.delayToDate = delayToDate;
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
        final Commands command = tx.findCommand(Commands.class, cmd -> true).getValue();

        // CREATE THE ESCROW SCHEDULE CONTRACT
        if (command instanceof Commands.CreateSchedule) { verifyCreate(tx); }

        // START A JOB ON THE SCHEDULE
        if (command instanceof Commands.StartJob) { startJobWithIndex(tx); }

        // CONTRACTOR DECLARES A STARTED JOB TO BE COMPLETE
        if (command instanceof Commands.DeclareJobComplete) { declareCompleteWithIndex(tx); }

        // EMPLOYERS SIGN-OFF A JOB MARKED AS COMPLETE
        if (command instanceof Commands.ConfirmJobComplete) { confirmJobCompleteWithIndex(tx); }

        // EMPLOYERS REJECT A JOB MARKED AS COMPLETE
        if (command instanceof Commands.ContinueJob) { orderContinuationOfJobWithIndex(tx); }

        // CONTRACTORS REQUEST CONTRACT AMOUNT AMENDMENT,
        // USING AN ATTACHMENT THAT IS WITHIN 'VARIABLES CLAUSE'
        if (command instanceof Commands.RequestExpectedDateModification) { requestDateAmendment(tx); }
    }


    private List<PublicKey> getOwningKeys(List<Party> parties) {
        List<PublicKey> keys = new ArrayList<PublicKey>();
        if(parties != null) {
            parties.forEach((party) -> keys.add(party.getOwningKey()));
        }
        return keys;
    }

    private void verifyCreate(LedgerTransaction tx) {
        // If the Create command isn’t present, or if the transaction has
        // multiple Create commands, an exception will be thrown
        // and contract verification will fail.
        final CommandWithParties<Commands.CreateSchedule> command = requireSingleCommand(tx.getCommands(), Commands.CreateSchedule.class);

        requireThat(require -> {
            require.using("Output state is a type of: 'ScheduleEscrowState'", tx.getOutputStates().get(0) instanceof ScheduleEscrowState);

            // Input/Output state requirements.
            require.using("No inputs should be consumed when issuing a Schedule.", tx.getInputs().isEmpty());
            require.using("There should be one output state.", tx.getOutputs().size() == 1);

            // State-specific requirements
            final ScheduleEscrowState jobOutput = tx.outputsOfType(ScheduleEscrowState.class).get(0);
            final List<Party> employers = jobOutput.getEmployers();
            final List<Party> contractors = jobOutput.getContractors();
            final List<PublicKey> expectedSigners = new ArrayList<>();
            expectedSigners.addAll(getOwningKeys(employers));
            expectedSigners.addAll(getOwningKeys(contractors));
            System.out.println("No. Expected Signers: " + expectedSigners.size());
            System.out.println("No. signers in command: " + command.getSigners().size());

            require.using("The employers and the contractors should be different parties.", !employers.containsAll(contractors));
            List<JCTJob> jobs = jobOutput.getJobs();
            require.using("Output state must have at least one Job", !jobs.isEmpty());
            require.using("All the jobs should be unstarted/pending.", jobs.stream().allMatch(job -> job.getStatus() == JCTJobStatus.PENDING));

            require.using("Testing for multiple employers & contractors", jobOutput.getParticipants().size() > 2);
            require.using("The employers and contractors should be required signers.",
                    command.getSigners().containsAll(expectedSigners));

            require.using("Contract Amount must be greater zero", jobOutput.getContractSum() > 0.0);

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
            int jobIndex = new Commands.StartJob(command.getValue().jobIx).jobIx;
            JCTJob inputModifiedJob = jobInputs.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutputs.getJobs().get(jobIndex);

            // Signers:
            final List<Party> employers = jobOutputs.getEmployers();
            final List<Party> contractors = jobOutputs.getContractors();
            final List<PublicKey> expectedSigners = new ArrayList<>();

            expectedSigners.addAll(getOwningKeys(employers));
            expectedSigners.addAll(getOwningKeys(contractors));
            require.using("The modified Job should have an input status of PENDING.",
                    inputModifiedJob.getStatus() == JCTJobStatus.PENDING);
            require.using("The Job should have an output status of IN_PROGRESS.",
                    outputModifiedJob.getStatus() == JCTJobStatus.IN_PROGRESS);
            require.using("The modified Job's description and amount shouldn't change.",
                    inputModifiedJob.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build().equals(outputModifiedJob));

            List<JCTJob> otherInputtedJobs = new ArrayList<>(jobInputs.getJobs());
            otherInputtedJobs.remove(jobIndex);
            List<JCTJob> otherOutputJobs = new ArrayList<>(jobOutputs.getJobs());
            otherOutputJobs.remove(jobIndex);

            require.using("All other jobs mustn't be changed",
                    IntStream.range(0, otherInputtedJobs.size()).allMatch(idx ->
                            (otherInputtedJobs.get(idx).equals(otherOutputJobs.get(idx)))
                    ));
            require.using("The employers and contractors should be required signers.",
                    command.getSigners().containsAll(expectedSigners));

            return null;
        });
    }

    private void declareCompleteWithIndex(LedgerTransaction tx) {
        final CommandWithParties<Commands.DeclareJobComplete> command = requireSingleCommand(tx.getCommands(), Commands.DeclareJobComplete.class);
        requireThat(require -> {
            require.using("One JobState input should be consumed.", tx.getInputs().size() == 1);
            require.using("One JobState output should be produced.", tx.getOutputs().size() == 1);

            ScheduleEscrowState jobInputs =  tx.inputsOfType(ScheduleEscrowState.class).get(0);
            ScheduleEscrowState jobOutputs =  tx.outputsOfType(ScheduleEscrowState.class).get(0);
            int jobIndex = new Commands.DeclareJobComplete(command.getValue().jobIx).jobIx;
            JCTJob inputModifiedJob = jobInputs.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutputs.getJobs().get(jobIndex);

            // Signers:
            final List<Party> expectedSigners = jobInputs.getContractors();

            require.using("The modified Job should have an input status of IN_PROGRESS.",
                    inputModifiedJob.getStatus() == JCTJobStatus.IN_PROGRESS);
            require.using("The Job should have an output status of COMPLETED.",
                    outputModifiedJob.getStatus() == JCTJobStatus.COMPLETED);
            require.using("The updated Job must not have a modified Job amount",
                    inputModifiedJob.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build().equalsExcept(outputModifiedJob, "Description"));
            require.using("The modified Job's description must include 'Quality Surveyor Link'.",
                    outputModifiedJob.getDescription().contains("Quality Surveyor Link"));

            List<JCTJob> otherInputtedJobs = new ArrayList<>(jobInputs.getJobs());
            otherInputtedJobs.remove(jobIndex);
            List<JCTJob> otherOutputJobs = new ArrayList<>(jobOutputs.getJobs());
            otherOutputJobs.remove(jobIndex);

            require.using("All other jobs mustn't be changed",
                    IntStream.range(0, otherInputtedJobs.size()).allMatch(idx ->
                            (otherInputtedJobs.get(idx).equals(otherOutputJobs.get(idx)))
                    ));
            require.using("At least a single contractor should be a required signer.",
                    expectedSigners.stream().anyMatch(signer ->
                            command.getSigners().contains(signer.getOwningKey())
                    ));

            return null;
        });
    }

    private void confirmJobCompleteWithIndex(LedgerTransaction tx) {
        final CommandWithParties<Commands.ConfirmJobComplete> command = requireSingleCommand(tx.getCommands(), Commands.ConfirmJobComplete.class);
        requireThat(require -> {
            require.using("One JobState input should be consumed.", tx.getInputs().size() == 1);
            require.using("One JobState output should be produced.", tx.getOutputs().size() == 1);

            ScheduleEscrowState jobInputs =  tx.inputsOfType(ScheduleEscrowState.class).get(0);
            ScheduleEscrowState jobOutputs =  tx.outputsOfType(ScheduleEscrowState.class).get(0);
            int jobIndex = new Commands.ConfirmJobComplete(command.getValue().jobIx).jobIx;
            JCTJob inputModifiedJob = jobInputs.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutputs.getJobs().get(jobIndex);

            // Signers:
            final List<Party> contractors = jobInputs.getContractors();
            final List<Party> employers = jobOutputs.getEmployers();

            final List<PublicKey> expectedInputSigners = new ArrayList<>();
            final List<PublicKey> expectedOutputSigners = new ArrayList<>();

            expectedInputSigners.addAll(getOwningKeys(contractors));
            expectedOutputSigners.addAll(getOwningKeys(employers));

            require.using("Input state must be contractor-signed.",
                    !expectedInputSigners.isEmpty());

            require.using("Output state must involve employer signatures.",
                    !expectedOutputSigners.isEmpty());

            require.using("The modified Job should have an input status of COMPLETED.",
                    inputModifiedJob.getStatus() == JCTJobStatus.COMPLETED);
            require.using("The Job should have an output status of CONFIRMED.",
                    outputModifiedJob.getStatus() == JCTJobStatus.CONFIRMED);

            require.using("The updated Job must not have a modified Job amount",
                    inputModifiedJob.copyBuilder().withStatus(JCTJobStatus.CONFIRMED).build().equals(outputModifiedJob));


            List<JCTJob> otherInputtedJobs = new ArrayList<>(jobInputs.getJobs());
            otherInputtedJobs.remove(jobIndex);
            List<JCTJob> otherOutputJobs = new ArrayList<>(jobOutputs.getJobs());
            otherOutputJobs.remove(jobIndex);

            require.using("All other jobs mustn't be changed",
                    IntStream.range(0, otherInputtedJobs.size()).allMatch(idx ->
                            (otherInputtedJobs.get(idx).equals(otherOutputJobs.get(idx)))
                    ));
            require.using("All employers should be required signers.",
                    command.getSigners().containsAll(expectedOutputSigners));
            require.using("All signers must be authorised via previous transactions.",
                    command.getSigners().equals(expectedOutputSigners));

            return null;
        });
    }

    private void orderContinuationOfJobWithIndex(LedgerTransaction tx) {
        final CommandWithParties<Commands.ContinueJob> command = requireSingleCommand(tx.getCommands(), Commands.ContinueJob.class);
        requireThat(require -> {
            require.using("One JobState input should be consumed.", tx.getInputs().size() == 1);
            require.using("One JobState output should be produced.", tx.getOutputs().size() == 1);

            ScheduleEscrowState jobInputs =  tx.inputsOfType(ScheduleEscrowState.class).get(0);
            ScheduleEscrowState jobOutputs =  tx.outputsOfType(ScheduleEscrowState.class).get(0);
            int jobIndex = new Commands.ContinueJob(command.getValue().jobIx).jobIx;
            JCTJob inputModifiedJob = jobInputs.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutputs.getJobs().get(jobIndex);

            // Signers:
            final List<Party> contractors = jobInputs.getContractors();
            final List<Party> employers = jobOutputs.getEmployers();

            final List<PublicKey> expectedInputSigners = new ArrayList<>();
            final List<PublicKey> expectedOutputSigners = new ArrayList<>();

            expectedInputSigners.addAll(getOwningKeys(contractors));
            expectedOutputSigners.addAll(getOwningKeys(employers));

            require.using("Input state must include contractors.",
                    !expectedInputSigners.isEmpty());

            require.using("Output state must involve employer signatures.",
                    !expectedOutputSigners.isEmpty());

            require.using("The modified Job should have an input status of COMPLETED.",
                    inputModifiedJob.getStatus() == JCTJobStatus.COMPLETED);
            require.using("The Job should have an output status of IN_PROGRESS.",
                    outputModifiedJob.getStatus() == JCTJobStatus.IN_PROGRESS);

            require.using("The updated Job must not have a modified Job amount",
                    inputModifiedJob.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build().equals(outputModifiedJob));


            List<JCTJob> otherInputtedJobs = new ArrayList<>(jobInputs.getJobs());
            otherInputtedJobs.remove(jobIndex);
            List<JCTJob> otherOutputJobs = new ArrayList<>(jobOutputs.getJobs());
            otherOutputJobs.remove(jobIndex);

            require.using("All other jobs mustn't be changed",
                    IntStream.range(0, otherInputtedJobs.size()).allMatch(idx ->
                            (otherInputtedJobs.get(idx).equals(otherOutputJobs.get(idx)))
                    ));
            require.using("All employers should be required signers.",
                    command.getSigners().containsAll(expectedOutputSigners));
            require.using("All signers must be authorised via previous transactions.",
                    command.getSigners().equals(expectedOutputSigners));

            return null;
        });
    }

    private void requestDateAmendment(LedgerTransaction tx) {
        final CommandWithParties<Commands.RequestExpectedDateModification> command = requireSingleCommand(tx.getCommands(), Commands.RequestExpectedDateModification.class);

        requireThat(require -> {
            require.using("Two inputs should be consumed.", tx.getInputs().size() == 2);
            require.using("Two outputs should be produced.", tx.getOutputs().size() == 2);

            List<ScheduleEscrowState> jobInputs =  tx.inputsOfType(ScheduleEscrowState.class);
            List<ReportState> reportInputs = tx.inputsOfType(ReportState.class);

            require.using("Must have one JobState input and one ReportState input",
                    !jobInputs.isEmpty() && !reportInputs.isEmpty());

            List<ScheduleEscrowState> jobOutputs =  tx.outputsOfType(ScheduleEscrowState.class);
            List<ReportState> reportOutputs = tx.outputsOfType(ReportState.class);

            require.using("Must have one JobState output and one ReportState output",
                    !jobOutputs.isEmpty() && !reportOutputs.isEmpty());

            // Report-specific verification:
            ReportState reportInput = reportInputs.get(0);
            ScheduleEscrowState jobInput = jobInputs.get(0);
            ReportState reportOutput = reportOutputs.get(0);
            ScheduleEscrowState jobOutput = jobOutputs.get(0);

            require.using("Report should have status: UNSEEN",
                    reportInput.getStatus() == ReportStatus.UNSEEN);

            require.using("Output Report should have status: PROCESSED",
                    reportOutput.getStatus() == ReportStatus.PROCESSED);

            // Job Specific verification
            int jobIndex = new Commands.RequestExpectedDateModification(command.getValue().jobIx, command.getValue().delayToDate).jobIx;
            LocalDate extendedDate = new Commands.RequestExpectedDateModification(command.getValue().jobIx, command.getValue().delayToDate).delayToDate;
            JCTJob inputModifiedJob = jobInput.getJobs().get(jobIndex);
            JCTJob outputModifiedJob = jobOutput.getJobs().get(jobIndex);

            // Signers:
            final List<Party> expectedSigners = jobInput.getContractors();

            require.using("Job amended should not change status",
                    outputModifiedJob.getStatus() == inputModifiedJob.getStatus());
            require.using("Expected End Date of job should be modified",
                    !inputModifiedJob.getExpectedEndDate().equals(outputModifiedJob.getExpectedEndDate()));
            require.using("Should Modify Expected End Date Correctly",
                    inputModifiedJob.copyBuilder().withExpectedEndDate(extendedDate).build().equals(outputModifiedJob));
            List<JCTJob> otherInputtedJobs = new ArrayList<>(jobInput.getJobs());
            otherInputtedJobs.remove(jobIndex);
            List<JCTJob> otherOutputJobs = new ArrayList<>(jobOutput.getJobs());
            otherOutputJobs.remove(jobIndex);

            require.using("All other jobs mustn't be changed",
                    IntStream.range(0, otherInputtedJobs.size()).allMatch(idx ->
                            (otherInputtedJobs.get(idx).equals(otherOutputJobs.get(idx)))
                    ));
            require.using("At least a single contractor should be a required signer.",
                    expectedSigners.stream().anyMatch(signer ->
                            command.getSigners().contains(signer.getOwningKey())
                    ));

            return null;
        });
    }
}
