package com.template.contracts;

import com.template.states.*;
import kotlin.Unit;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class CreateScheduleEscrowContractTests {
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

    private ScheduleClauseState getScheduleEscrow(boolean empIsCon, List<JCTJob> jobs) {
        if (jobs == null) {
            jobs = jobFactory.getJobExamples();
        }
        if (empIsCon) {
            return new ScheduleClauseState(
                    "Project Title",
                    employers,
                    employers,
                    1000.0,
                    1.0, jobs);
        }
        return new ScheduleClauseState(
                "Project Title",
                employers,
                contractors,
                1000.0,
                1.0, jobs);
    }

    @Test
    public void shouldCreateEscrowStateSuccessfully() {
        ScheduleClauseState outputState = getScheduleEscrow(false,null);
        System.out.println("jobs.isEmpty(): " + outputState.getJobs().isEmpty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldRejectAnyInputStates() {
        ScheduleClauseState inputState = getScheduleEscrow(false, null);
        ScheduleClauseState outputState = getScheduleEscrow(false, null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.input(com.template.contracts.ScheduleClauseContract.ID, inputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("No inputs should be consumed when issuing a Schedule.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void shouldCreateSingleOutputState() {
        ScheduleClauseState outputState = getScheduleEscrow(false, null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("There should be one output state.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void employersDifferFromContractors() {
        ScheduleClauseState outputState = getScheduleEscrow(true, null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The employers and the contractors should be different parties.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void outputStateMustBeScheduleEscrowState() {
        JCTMasterState outputState = new JCTMasterState("Project 1", employers.get(0), contractors.get(0));
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Output state is a type of: 'ScheduleEscrowState'");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void allJobsInOutputStateMustBePending() {
        JCTJob inProgressJob1 = new JCTJob(null,null,null,null,null, JCTJobStatus.PENDING);
        JCTJob inProgressJob2 = new JCTJob(null,null,null,null,null,JCTJobStatus.IN_PROGRESS);
        List<JCTJob> inProgressJobs = Arrays.asList(inProgressJob1, inProgressJob2);
        ScheduleClauseState outputState = getScheduleEscrow(false, inProgressJobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("All the jobs should be unstarted/pending.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void mustHaveAtLeastOneNonEmptyJobInSchedule() {
        List<JCTJob> emptyJobs = Collections.emptyList();
        ScheduleClauseState outputState = getScheduleEscrow(false, emptyJobs);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("Must have at least one Job");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void allEmployersAndContractorsMustBeIncludedInTransaction() {
        ScheduleClauseState outputState = getScheduleEscrow(false, null);
        List<PublicKey> employerKeys = Arrays.asList(employers.get(0).getOwningKey(), employers.get(0).getOwningKey());
        List<PublicKey> contractorKeys = Arrays.asList(contractors.get(0).getOwningKey(), contractors.get(0).getOwningKey());
        System.out.println("LIST of Employers: " + employerKeys.size());
        System.out.println("LIST of Contractors: " + contractorKeys.size());
        System.out.println("LIST of Required: " + requiredSigners.size());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(employerKeys, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The employers and contractors should be required signers.");
            });
            l.transaction(tx -> {
                tx.command(contractorKeys, new ScheduleClauseContract.Commands.CreateSchedule());
                tx.output(com.template.contracts.ScheduleClauseContract.ID, outputState);
                return tx.failsWith("The employers and contractors should be required signers.");
            });
            return Unit.INSTANCE;
        });

    }
}