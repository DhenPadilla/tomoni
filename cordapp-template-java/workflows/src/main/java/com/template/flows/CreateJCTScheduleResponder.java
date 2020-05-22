package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.ScheduleClauseState;
import kotlin.Unit;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(CreateJCTScheduleFlow.class)
public class CreateJCTScheduleResponder extends FlowLogic<Unit> {
    private final FlowSession otherPartySession;

    public CreateJCTScheduleResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public Unit call() throws FlowException {
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartySession) {
                super(otherPartySession);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be an ScheduleEscrowState transaction - this transaction is not an 'ScheduleEscrowState'",
                            output instanceof ScheduleClauseState);
                    ScheduleClauseState scheduleEscrowState = (ScheduleClauseState) output;
                    require.using("JCT Project name must be given", !scheduleEscrowState.getProjectName().isEmpty());
                    require.using("ScheduleEscrow must have a Contract Sum", !scheduleEscrowState.getContractSum().isNaN());
                    return null;
                });
            }
        }

        SecureHash expectedTxId = subFlow(new SignTxFlow(otherPartySession)).getId();

        // Responder flow logic goes here.
        subFlow(new ReceiveFinalityFlow(otherPartySession, expectedTxId));

        return null;
    }
}
