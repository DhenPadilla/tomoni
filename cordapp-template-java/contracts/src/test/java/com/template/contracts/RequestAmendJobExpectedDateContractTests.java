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
    private TestIdentity unauthorisedEmployer = new TestIdentity(new CordaX500Name("Unauthorised Employer", "London", "GB"));
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
    JCTJob job1Output = job1.copyBuilder()
            .withStatus(JCTJobStatus.IN_PROGRESS)
            .withExpectedEndDate(job1.getExpectedEndDate().plusMonths(3))
            .build();

    private ScheduleEscrowState getScheduleEscrowState(ScheduleEscrowState state) {
        List<JCTJob> jobs;
        if (state == null) {
            jobs = Arrays.asList(job1, job2);
            return new ScheduleEscrowState(
                    "Project Title",
                    employers,
                    contractors,
                    1000.0,
                    1.0, jobs);
        }
        else {
            // Copy the state given with updated jobs
            jobs = Arrays.asList(job1Output, job2);
            return state.copyWithNewJobs(jobs);
        }
    }

    private ReportState getReportState(ReportStatus status) {
        return new ReportState(status, "J1", Instant.now(), "Lorem ipsum", contractors);
    }

    @Test
    public void confirmAmendJobShouldWork() {
        ReportState inputReportState = getReportState(ReportStatus.UNSEEN);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, inputReportState);
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });

    }

    @Test
    public void requestAmendJobShouldHaveTwoInputs() {
        ReportState outputReportState = getReportState(ReportStatus.UNSEEN);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputReportState);
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                tx.input(ReportContract.ID, outputReportState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("Two inputs should be consumed.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveTwoOutputs() {
        ReportState outputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, outputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Two outputs should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveOneReportAndOneJobInput() {
        ReportState outputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveOneReportAndOneJobOutput() {
        ReportState outputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", outputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void attachedReportContractMustHaveStatusUNSEEN() {
        ReportState inputReportState = getReportState(ReportStatus.UNSEEN);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", inputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, outputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Report should have status: UNSEEN");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputReportContractMustHaveStatusPROCESSED() {
        ReportState inputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
//        ReportState outputReportState = new ReportState(ReportStatus.PROCESSED, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, inputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Output Report should have status: PROCESSED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeAmendedMustNotChangeStatus() {
        ReportState inputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ReportState outputReportState = new ReportState(ReportStatus.PROCESSED, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build(), job2);
        ScheduleEscrowState outputState = inputState.copyWithNewJobs(jobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Job amended should not change status");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldModifyExpectedEndDate() {
        ReportState inputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ReportState outputReportState = new ReportState(ReportStatus.PROCESSED, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate();
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build(), job2);
        ScheduleEscrowState outputState = inputState.copyWithNewJobs(jobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Expected End Date of job should be modified");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldModifyExpectedEndDateCorrectly() {
        ReportState inputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ReportState outputReportState = new ReportState(ReportStatus.PROCESSED, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(1);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("Should Modify Expected End Date Correctly");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void atLeastSingleContractorMustBeIncludedInTransaction() {
        List<PublicKey> employerKeys = Arrays.asList(employers.get(0).getOwningKey(), employers.get(1).getOwningKey());
        ReportState inputReportState = new ReportState(ReportStatus.UNSEEN, "J1", Instant.now(), "Lorem ipsum", contractors);
        ReportState outputReportState = new ReportState(ReportStatus.PROCESSED, "J1", Instant.now(), "Lorem ipsum", contractors);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        LocalDate extendedDate = inputState.getJobs().get(0).getExpectedEndDate().plusMonths(3);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(employerKeys, new ScheduleEscrowContract.Commands.RequestExpectedDateModification(0, extendedDate));
                return tx.failsWith("At least a single contractor should be a required signer.");
            });
            return Unit.INSTANCE;
        });

    }
}