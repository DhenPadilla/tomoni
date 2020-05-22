package com.template.contracts;

import com.template.states.JCTJob;
import com.template.states.JCTJobStatus;
import com.template.states.ScheduleClauseState;
import kotlin.Unit;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class ContinueScheduledJobContractTests {
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
    JCTJob job1 = jobFactory.getJobExamples().get(0).copyBuilder().withStatus(JCTJobStatus.COMPLETED).build();
    JCTJob job2 = jobFactory.getJobExamples().get(1).copyBuilder().withStatus(JCTJobStatus.COMPLETED).build();
    JCTJob job1Complete = job1.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();

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
            jobs = Arrays.asList(job1Complete, job2);
            return state.copyBuilder().withJobs(jobs).build();
        }
    }

    @Test
    public void continueJobShouldWork() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void mustHaveOneOutputAndInputState() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("One JobState input should be consumed.");
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                return tx.failsWith("One JobState output should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void inputStateMustHaveContractorsAsSigners() {
        ScheduleClauseState inputState = new ScheduleClauseState(
                "Project Title",
                employers,
                null,
                1000.0,
                1.0,
                Arrays.asList(job1, job2));
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Input state must include contractors.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputStateMustHaveEmployersAsSigners() {
        ScheduleClauseState inputState = new ScheduleClauseState(
                "Project Title",
                null,
                contractors,
                1000.0,
                1.0,
                Arrays.asList(job1, job2));
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Output state must involve employer signatures.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveInputStatusCOMPLETED() {
        ScheduleClauseState unusedState = getScheduleEscrowState(null);
        List<JCTJob> newInputJobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build(), job2);
        ScheduleClauseState inputState = unusedState.copyBuilder().withJobs(newInputJobs).build();
        ScheduleClauseState outputState = getScheduleEscrowState(null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The modified Job should have an input status of COMPLETED.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveOutputStatusINPROGRESS() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The Job should have an output status of IN_PROGRESS.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void updatedJobShouldNotAllowModifiedAmount() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        JCTJob outputJob1 = job1Complete.copyBuilder().withAmount(job1.getAmount() + 100.0).build();
        List<JCTJob> outputJobs = Arrays.asList(outputJob1, job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(outputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The updated Job must not have a modified Job amount");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void otherJobsShouldNotBeUpdated() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        JCTJob updatedJob2 = job2.copyBuilder().withStatus(JCTJobStatus.CONFIRMED).build();
        List<JCTJob> outputJobs = Arrays.asList(job1Complete, updatedJob2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(outputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("All other jobs mustn't be changed");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void allEmployersMustBeIncludedInTransaction() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        List<PublicKey> employerKeys = Arrays.asList(employer1.getPublicKey());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("All employers should be required signers.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void transactionFailsWhenSignerIncludesUnauthorisedEntity() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        List<PublicKey> employerKeys = Arrays.asList(employer1.getPublicKey(), employer2.getPublicKey(), unauthorisedEmployer.getPublicKey());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleClauseContract.Commands.ContinueJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("All signers must be authorised via previous transactions.");
            });
            return Unit.INSTANCE;
        });
    }
}