package com.template.contracts;

import com.template.states.*;
import kotlin.Unit;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class RequestAmendJobExpectedDateContractTests {
    private TestIdentity employer1 = new TestIdentity(new CordaX500Name("Employer1", "London", "GB"));
    private TestIdentity employer2 = new TestIdentity(new CordaX500Name("Employer2", "London", "GB"));
    List<Party> employers = Arrays.asList(employer1.getParty(), employer2.getParty());
    private TestIdentity contractor1 = new TestIdentity(new CordaX500Name("Contractor1", "London", "GB"));
    private TestIdentity contractor2 = new TestIdentity(new CordaX500Name("Contractor2", "London", "GB"));
    List<Party> contractors = Arrays.asList(contractor1.getParty(), contractor2.getParty());
    List<PublicKey> requiredSigners = Arrays.asList(contractor1.getPublicKey(), contractor1.getPublicKey());

    private final MockServices ledgerServices =
            new MockServices(Arrays.asList("com.template.contracts"),
                    employer1, employer2, contractor1, contractor2);

    JobExamples jobFactory = new JobExamples();
    JCTJob job1 = jobFactory.getJobExamples().get(0).copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();
    JCTJob job2 = jobFactory.getJobExamples().get(1).copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();
    Instant surveyDate = Instant.now();
    LocalDate requestCompletionDate = job1.getExpectedEndDate().plusMonths(3);
    Double requestedContractSum = null;
    JCTJob job1Output = job1.copyBuilder()
            .withStatus(JCTJobStatus.DATE_AMENDMENT_REQUESTED)
            .build();

    private ScheduleClauseState getScheduleEscrowState(ScheduleClauseState state) {
        List<JCTJob> jobs;
        if (state == null) {
            jobs = Arrays.asList(job1, job2);
            return new ScheduleClauseState(
                    "Project Title",
                    employers,
                    contractors,
                    1000.0,
                    1.0, jobs);
        }
        else {
            // Copy the state given with updated jobs
            jobs = Arrays.asList(job1Output, job2);
            return state.copyBuilder().withJobs(jobs).build();
        }
    }

    private ReportState getNullRequestReportState(ReportStatus status) {
        return new ReportState(status, "J1", surveyDate, null, null, "Lorem ipsum", contractors);
    }

    private ReportState getReportState(ReportStatus status) {
        return new ReportState(status, "J1", surveyDate, requestCompletionDate, requestedContractSum, "Lorem ipsum", contractors);
    }

    @Test
    public void confirmAmendDateShouldWork() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = job1.getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, inputReportState);
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });

    }

    @Test
    public void requestAmendJobShouldHaveTwoInputs() {
        ReportState outputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputReportState);
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                tx.input(ReportContract.ID, outputReportState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Two inputs should be consumed.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveTwoOutputs() {
        ReportState outputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, outputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Two outputs should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveOneReportAndOneJobInput() {
        ReportState outputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveOneReportAndOneJobOutput() {
        ReportState outputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void attachedReportContractMustHaveStatusUNSEEN() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", inputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, outputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Report should have status: UNSEEN");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputReportContractMustHaveStatusPROCESSED() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, inputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Output Report should have status: PROCESSED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobInputForRequestDateModificationShouldHaveStatusINPROGRESS() {
        // Valid input Report state with status: ISSUED
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        // Valid output Report state with status: PROCESSED
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        // Invalid input Schedule Clause state with job status: PENDING
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.DATE_AMENDMENT_REQUESTED).build(), job2);
        // Valid output Schedule Clause state with job status: DATE_AMENDMENT_REQUESTED
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        // Prepare the RequestExpectedDateModification ledger transaction with the input/output states above
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                // Execute the transaction with the states above, and the correct set of signers
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(
                        0, requestCompletionDate));
                // Test should correctly fail with the following error:
                return tx.failsWith("Input Job should have status: IN_PROGRESS");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobOutputShouldHaveStatusDATEREQUESTED() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build(), job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, requestCompletionDate));
                return tx.failsWith("Output Job should have status: DATE_AMENDMENT_REQUESTED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldNotModifyScheduleEscrowStateExpectedEndDate() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1Output.copyBuilder().withExpectedEndDate(requestCompletionDate).build(), job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, requestCompletionDate));
                return tx.failsWith("Input ScheduleEscrowState should not change besides Status");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldSaveExpectedEndDateInReportStateCorrectly() {
        ReportState inputReportState = getNullRequestReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getNullRequestReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1Output, job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, requestCompletionDate));
                return tx.failsWith("Expected End Date of job should be saved in output ReportState");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void atLeastSingleContractorMustBeIncludedInTransaction() {
        List<PublicKey> employerKeys = Arrays.asList(employers.get(0).getOwningKey(), employers.get(1).getOwningKey());
        ReportState inputReportState = getNullRequestReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(employerKeys, new ScheduleClauseContract.Commands.RequestExpectedDateModification(0, requestCompletionDate));
                return tx.failsWith("At least a single contractor should be a required signer.");
            });
            return Unit.INSTANCE;
        });

    }
}