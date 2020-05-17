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
import java.util.Arrays;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class AcceptAmendJobAmountContractTests {
    private TestIdentity employer1 = new TestIdentity(new CordaX500Name("Employer1", "London", "GB"));
    private TestIdentity employer2 = new TestIdentity(new CordaX500Name("Employer2", "London", "GB"));
    private TestIdentity unauthorisedEmployer = new TestIdentity(new CordaX500Name("Unauthorised Employer", "London", "GB"));
    List<Party> employers = Arrays.asList(employer1.getParty(), employer2.getParty());
    private TestIdentity contractor1 = new TestIdentity(new CordaX500Name("Contractor1", "London", "GB"));
    private TestIdentity contractor2 = new TestIdentity(new CordaX500Name("Contractor2", "London", "GB"));
    List<Party> contractors = Arrays.asList(contractor1.getParty(), contractor2.getParty());
    List<PublicKey> requiredSigners = Arrays.asList(employer1.getPublicKey(), employer2.getPublicKey());

    private final MockServices ledgerServices =
            new MockServices(Arrays.asList("com.template.contracts"),
                    employer1, employer2, contractor1, contractor2);

    JobExamples jobFactory = new JobExamples();
    JCTJob job1 = jobFactory.getJobExamples().get(0).copyBuilder().withStatus(JCTJobStatus.AMOUNT_AMENDMENT_REQUESTED).build();
    JCTJob job2 = jobFactory.getJobExamples().get(1).copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();
    Instant surveyDate = Instant.now();
    Double requestedAmount = 200.0;
    Double newAmount = requestedAmount + job1.getAmount();
    JCTJob job1Output = job1.copyBuilder()
            .withStatus(JCTJobStatus.IN_PROGRESS)
            .withAmount(newAmount)
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
            return state.copyBuilder().withJobs(jobs).withContractSum(1200.0).build();
        }
    }

    private ReportState getRequestReportState(ReportStatus status) {
        return new ReportState(status, "J1", surveyDate, null, requestedAmount, "Lorem ipsum", contractors);
    }

    private ReportState getOutputReportState(ReportStatus status) {
        return new ReportState(status, "J1", surveyDate, null, requestedAmount, "Lorem ipsum", contractors);
    }

    @Test
    public void confirmAmendDateShouldWork() {
        ReportState inputReportState = getRequestReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });

    }

    @Test
    public void acceptAmendJobShouldHaveTwoInputs() {
        ReportState outputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                tx.input(ReportContract.ID, outputReportState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("Two inputs should be consumed.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void acceptAmendJobShouldHaveTwoOutputs() {
        ReportState outputReportState = getOutputReportState(ReportStatus.ISSUED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
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
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Two outputs should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void acceptAmendJobShouldHaveOneReportAndOneJobInput() {
        ReportState outputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void acceptAmendJobShouldHaveOneReportAndOneJobOutput() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Must have one JobState input and one ReportState input");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void inputReportContractMustHaveStatusPROCESSED() {
        ReportState inputReportState = getRequestReportState(ReportStatus.ISSUED);
        ReportState outputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Report should have status: PROCESSED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputReportContractMustHaveStatusCONSUMED() {
        ReportState inputReportState = getRequestReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Output Report should have status: CONSUMED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobInputShouldHaveStatusAMOUNTREQUESTED() {
        ReportState inputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build(), job2);
        ScheduleEscrowState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, outputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Input Job should have status: AMOUNT_AMENDMENT_REQUESTED");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobOutputShouldHaveStatusINPROGRESS() {
        ReportState inputReportState = getOutputReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.COMPLETED).build(), job2);
        ScheduleEscrowState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Output Job should have status: IN_PROGRESS");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldModifyAmountInScheduleEscrowStateJobCorrectly() {
        ReportState inputReportState = getRequestReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        List<JCTJob> jobs = Arrays.asList(job1Output.copyBuilder()
                .withAmount(job1Output.getAmount() - 100.0).build(), job2);
        ScheduleEscrowState outputState = inputState.copyBuilder().withJobs(jobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Output Job should have same amount as requested in report");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldModifyContractSumInScheduleEscrowStateCorrectly() {
        ReportState inputReportState = getRequestReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState modifiedState = getScheduleEscrowState(inputState);
        ScheduleEscrowState outputState = modifiedState.copyBuilder().withContractSum(modifiedState.getContractSum() - 100.0).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("Output ScheduleEscrowState should have same ContractSum as requested in report");
            });
            return Unit.INSTANCE;
        });
    }


    @Test
    public void allEmployersMustBeIncludedInTransaction() {
        List<PublicKey> employerKeys = Arrays.asList(employers.get(1).getOwningKey());
        ReportState inputReportState = getRequestReportState(ReportStatus.PROCESSED);
        ReportState outputReportState = getOutputReportState(ReportStatus.CONSUMED);
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        System.out.println(outputState.getContractSum());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ReportContract.ID, inputReportState);
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                tx.output(ReportContract.ID, outputReportState);
                tx.command(employerKeys, new ScheduleEscrowContract.Commands.AcceptAmountModification(0));
                return tx.failsWith("All authorised employers should be required signers.");
            });
            return Unit.INSTANCE;
        });

    }
}