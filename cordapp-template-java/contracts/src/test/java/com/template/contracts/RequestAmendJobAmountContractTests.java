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

public class RequestAmendJobAmountContractTests {
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
    Instant surveyDate = Instant.now();
    Double amountRequest = 200.0;
    JCTJob job1Output = job1.copyBuilder()
            .withStatus(JCTJobStatus.AMOUNT_AMENDMENT_REQUESTED)
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
        return new ReportState(status, "J1", surveyDate, null, amountRequest, "Lorem ipsum", contractors);
    }

    @Test
    public void confirmRequestAmendAmountShouldWork() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });

    }

    @Test
    public void requestAmendJobShouldHaveTwoInputs() {
        ReportState outputReportState = getReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputReportState);
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                tx.input(ReportContract.ID, outputReportState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Two inputs should be consumed.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void requestAmendJobShouldHaveTwoOutputs() {
        ReportState inputReportState = getNullRequestReportState(ReportStatus.ISSUED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ReportContract.ID, "J1 Report 1", inputReportState);
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                return tx.verifies();
            });
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
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
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, inputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("Output Report should have status: PROCESSED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobInputShouldHaveStatusINPROGRESS() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build(), job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("Input Job should have status: IN_PROGRESS");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobOutputShouldHaveStatusAMOUNTREQUESTED() {
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("Output Job should have status: AMOUNT_AMENDMENT_REQUESTED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldNotModifyScheduleEscrowStateJobAmount() {
        ReportState inputReportState = getReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1Output.copyBuilder().withAmount(amountRequest).build(), job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("ScheduleEscrowState should not change besides Status");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldSaveRequestedAmountInReportStateCorrectly() {
        ReportState inputReportState = getNullRequestReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getNullRequestReportState(ReportStatus.PROCESSED);
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("Output ReportState must have correctly saved contractSum");
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
                tx.command(employerKeys, new ScheduleClauseContract.Commands.RequestAmountModification(0, amountRequest));
                return tx.failsWith("At least a single contractor should be a required signer.");
            });
            return Unit.INSTANCE;
        });
    }
}