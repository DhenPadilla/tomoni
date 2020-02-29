package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.JCTState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(JCTFlow.class)
public class JCTFlowResponder extends FlowLogic<Void> {
    private final FlowSession employerSession;

    public JCTFlowResponder(FlowSession employerSession) {
        this.employerSession = employerSession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartySession) {
                super(otherPartySession);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be an IOU transaction - this transaction is not an 'IOU'", output instanceof JCTState);
                    JCTState jct = (JCTState) output;
                    require.using("JCT must have project name", !jct.getProjectName().isEmpty());
                    return null;
                });
            }
        }

        SecureHash expectedTxId = subFlow(new SignTxFlow(employerSession)).getId();

        // Responder flow logic goes here.
        subFlow(new ReceiveFinalityFlow(employerSession, expectedTxId));

        return null;
    }
}
