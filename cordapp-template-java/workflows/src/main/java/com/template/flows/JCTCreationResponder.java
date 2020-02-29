package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.JCTMasterState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(JCTCreationFlow.class)
public class JCTCreationResponder extends FlowLogic<Void> {
    private final FlowSession otherPartySession;

    public JCTCreationResponder(FlowSession otherPartySession) {
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
                    require.using("This must be an JCT Creation transaction - this transaction is not an 'JCT Creation'",
                            output instanceof JCTMasterState);
                    JCTMasterState jct = (JCTMasterState) output;
                    require.using("JCT Project name must be given", !jct.getProjectName().isEmpty());
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
