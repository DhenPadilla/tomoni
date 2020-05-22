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

public class StartScheduleJobContractTests {
    private TestIdentity employer1 = new TestIdentity(new CordaX500Name("Employer1", "London", "GB"));
    private TestIdentity employer2 = new TestIdentity(new CordaX500Name("Employer2", "London", "GB"));
    List<Party> employers = Arrays.asList(employer1.getParty(), employer2.getParty());
    private TestIdentity contractor1 = new TestIdentity(new CordaX500Name("Contractor1", "London", "GB"));
    private TestIdentity contractor2 = new TestIdentity(new CordaX500Name("Contractor2", "London", "GB"));
    List<Party> contractors = Arrays.asList(contractor1.getParty(), contractor2.getParty());
    List<PublicKey> requiredSigners = Arrays.asList(employer1.getPublicKey(), employer2.getPublicKey(), contractor1.getPublicKey(), contractor2.getPublicKey());
    private final MockServices ledgerServices =
            new MockServices(Arrays.asList("com.template.contracts"),
                    employer1, employer2, contractor1, contractor2);

    JobExamples jobFactory = new JobExamples();
    JCTJob job1 = jobFactory.getJobExamples().get(0);
    JCTJob job2 = jobFactory.getJobExamples().get(1);
    JCTJob job1InProgress = job1.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();
    JCTJob job2InProgress = job2.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build();

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
            jobs = Arrays.asList(job1InProgress, job2);
            return state.copyBuilder().withJobs(jobs).build();
        }
    }

    @Test
    public void startScheduleJobShouldWork() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
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
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("One JobState input should be consumed.");
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                return tx.failsWith("One JobState output should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveInputStatusPENDING() {
        ScheduleClauseState unusedState = getScheduleEscrowState(null);
        List<JCTJob> newInputJobs = Arrays.asList(job1InProgress, job2InProgress);
        ScheduleClauseState inputState = unusedState.copyBuilder().withJobs(newInputJobs).build();
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(newInputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The modified Job should have an input status of PENDING.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveOutputStatusINPROGRESS() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        List<JCTJob> outputJobs = Arrays.asList(job1, job2InProgress);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(outputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The Job should have an output status of IN_PROGRESS.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void updatedJobDescriptionAndAmountShouldNotChange() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        JCTJob outputJob1 = job1InProgress.copyBuilder().withDescription("THIS IS AN INCORRECT DESCRIPTION").withAmount(10.0).build();
        List<JCTJob> outputJobs = Arrays.asList(outputJob1, job2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(outputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The modified Job's description and amount shouldn't change.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void otherJobsShouldNotBeUpdated() {
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        JCTJob updatedJob2 = job2.copyBuilder().withDescription("BREAKING UPDATE").build();
        List<JCTJob> outputJobs = Arrays.asList(job1InProgress, updatedJob2);
        ScheduleClauseState outputState = inputState.copyBuilder().withJobs(outputJobs).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("All other jobs mustn't be changed");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void allEmployersAndContractorsMustBeIncludedInTransaction() {
        // Valid input Schedule Clause state
        ScheduleClauseState inputState = getScheduleEscrowState(null);
        // Valid output Schedule Clause state
        ScheduleClauseState outputState = getScheduleEscrowState(inputState);
        // Valid set of required employer signatures
        List<PublicKey> employerKeys = Arrays.asList(employers.get(0).getOwningKey(), employers.get(0).getOwningKey());
        // Valid set of required contractor signatures
        List<PublicKey> contractorKeys = Arrays.asList(contractors.get(0).getOwningKey(), contractors.get(0).getOwningKey());
        ledger(ledgerServices, l -> {
            // Failing transaction that is only signed by employers
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The employers and contractors should be required signers.");
            });
            // Failing transaction that is only signed by contractors
            l.transaction(tx -> {
                tx.command(contractorKeys, new ScheduleClauseContract.Commands.StartJob(0));
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The employers and contractors should be required signers.");
            });
            return Unit.INSTANCE;
        });

    }
}