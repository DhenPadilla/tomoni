package com.template.contracts;

import com.template.states.JCTJob;
import com.template.states.JCTJobStatus;
import com.template.states.ScheduleEscrowState;
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

public class ConfirmScheduleJobCompleteContractTests {
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
    JCTJob job1Complete = job1.copyBuilder().withStatus(JCTJobStatus.CONFIRMED).build();

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
            jobs = Arrays.asList(job1Complete, job2);
            return state.copyWithNewJobs(jobs);
        }
    }

    @Test
    public void confirmJobCompleteShouldWork() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void mustHaveOneOutputAndInputState() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("One JobState input should be consumed.");
            });
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                return tx.failsWith("One JobState output should be produced.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void inputStateMustHaveContractorsAsSigners() {
        ScheduleEscrowState inputState = new ScheduleEscrowState(
                "Project Title",
                employers,
                null,
                1000.0,
                1.0,
                Arrays.asList(job1, job2));
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("Input state must be contractor-signed.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputStateMustHaveEmployersAsSigners() {
        ScheduleEscrowState inputState = new ScheduleEscrowState(
                "Project Title",
                null,
                contractors,
                1000.0,
                1.0,
                Arrays.asList(job1, job2));
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("Output state must involve employer signatures.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveInputStatusCOMPLETED() {
        ScheduleEscrowState unusedState = getScheduleEscrowState(null);
        List<JCTJob> newInputJobs = Arrays.asList(job1.copyBuilder().withStatus(JCTJobStatus.IN_PROGRESS).build(), job2);
        ScheduleEscrowState inputState = unusedState.copyWithNewJobs(newInputJobs);
        ScheduleEscrowState outputState = getScheduleEscrowState(null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("The modified Job should have an input status of COMPLETED.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void jobToBeUpdatedMustHaveOutputStatusCONFIRMED() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("The Job should have an output status of CONFIRMED.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void updatedJobShouldNotAllowModifiedAmount() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        JCTJob outputJob1 = job1Complete.copyBuilder().withAmount(job1.getAmount() + 100.0).build();
        List<JCTJob> outputJobs = Arrays.asList(outputJob1, job2);
        ScheduleEscrowState outputState = inputState.copyWithNewJobs(outputJobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("The updated Job must not have a modified Job amount");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void otherJobsShouldNotBeUpdated() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        JCTJob updatedJob2 = job2.copyBuilder().withStatus(JCTJobStatus.CONFIRMED).build();
        List<JCTJob> outputJobs = Arrays.asList(job1Complete, updatedJob2);
        ScheduleEscrowState outputState = inputState.copyWithNewJobs(outputJobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("All other jobs mustn't be changed");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void allEmployersMustBeIncludedInTransaction() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        List<PublicKey> employerKeys = Arrays.asList(employer1.getPublicKey());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("All employers should be required signers.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void transactionFailsWhenSignerIncludesUnauthorisedEntity() {
        ScheduleEscrowState inputState = getScheduleEscrowState(null);
        ScheduleEscrowState outputState = getScheduleEscrowState(inputState);
        List<PublicKey> employerKeys = Arrays.asList(employer1.getPublicKey(), employer2.getPublicKey(), unauthorisedEmployer.getPublicKey());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleEscrowContract.Commands.ConfirmJobComplete(0));
                tx.input(ScheduleEscrowContract.ID, inputState);
                tx.output(ScheduleEscrowContract.ID, outputState);
                return tx.failsWith("All signers must be authorised via previous transactions.");
            });
            return Unit.INSTANCE;
        });
    }
}