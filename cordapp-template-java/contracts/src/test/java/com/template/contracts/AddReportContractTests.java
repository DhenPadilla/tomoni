package com.template.contracts;

import com.template.states.ReportState;
import com.template.states.ReportStatus;
import com.template.states.ScheduleEscrowState;
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

// TODO - Check for non-null 'request'
//  variables when creating a new report

public class AddReportContractTests {
    private TestIdentity employer1 = new TestIdentity(new CordaX500Name("Employer1", "London", "GB"));
    private TestIdentity employer2 = new TestIdentity(new CordaX500Name("Employer2", "London", "GB"));
    private TestIdentity contractor1 = new TestIdentity(new CordaX500Name("Contractor1", "London", "GB"));
    private TestIdentity contractor2 = new TestIdentity(new CordaX500Name("Contractor2", "London", "GB"));
    List<Party> reporters
            = Arrays.asList(
                    employer1.getParty(),
                    contractor1.getParty(), contractor2.getParty());
    List<PublicKey> requiredSigners
            = Arrays.asList(
                    employer1.getPublicKey(), employer2.getPublicKey(),
                    contractor1.getPublicKey(), contractor2.getPublicKey());

    private final MockServices ledgerServices =
            new MockServices(Arrays.asList("com.template.contracts"),
                    employer1, employer2, contractor1, contractor2);

    private ReportState getReportStateWith(String jctJobRef, Instant dateOfSurvey, LocalDate requestCompletionDate, Double requestContractSum, String reportBody) {
        return new ReportState(ReportStatus.UNSEEN, jctJobRef, dateOfSurvey, requestCompletionDate, requestContractSum, reportBody, reporters);
    }

    @Test
    public void confirmCreateReportShouldWork() {
        ReportState outputState = getReportStateWith("J1", Instant.now(), null, null, "Lorem ipsum");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputState);
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void contractRejectsNonReportStates() {
        ScheduleEscrowState outputState = new ScheduleEscrowState(null, null,null,null,null,null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputState);
                return tx.failsWith("Output state is a type of: 'ReportState'");
            });
            return Unit.INSTANCE;
        });
    }


    @Test
    public void reportContractRejectsInputState() {
        ReportState outputState = getReportStateWith("J1", Instant.now(), null, null,"Lorem ipsum");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.input(ReportContract.ID, outputState);
                tx.output(ReportContract.ID, outputState);
                return tx.failsWith("No input state must be consumed when creating a report.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void reportContractProducesSingleInputState() {
        ReportState outputState = getReportStateWith("J1", Instant.now(), null, null,"Lorem ipsum");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputState);
                tx.output(ReportContract.ID, outputState);
                return tx.failsWith("Single report state must be output in transaction.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void reportContractRejectsEmptyReport() {
        ReportState outputState = getReportStateWith(null, Instant.now(), null, null,null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputState);
                return tx.failsWith("Report must not have empty inputs");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void creationOfReportMustHaveStatusUNSEEN() {
        ReportState unusedState = getReportStateWith("J1", Instant.now(),null, null,"Lorem Ipsum");
        ReportState outputState = unusedState.copyBuilder().withStatus(ReportStatus.PROCESSED).build();
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(requiredSigners, new ReportContract.Commands.AddReportDocument());
                tx.output(ReportContract.ID, outputState);
                return tx.failsWith("Output ReportState must have status: UNSEEN");
            });
            return Unit.INSTANCE;
        });
    }

}