package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.IOUState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.hibernate.Transaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(IOUFlow.class)
public class IOUFlowResponder extends FlowLogic<Void> {
    private final FlowSession otherPartySession;

    public IOUFlowResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
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
                    require.using("This must be an IOU transaction - this transaction is not an 'IOU'", output instanceof IOUState);
                    IOUState iou = (IOUState) output;
                    require.using("IOU Value must not be higher than 100", iou.getValue() < 100);
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
