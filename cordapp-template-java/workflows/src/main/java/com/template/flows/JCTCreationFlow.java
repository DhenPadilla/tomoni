package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.IOUContract;
import com.template.contracts.JCTMasterContract;
import com.template.states.IOUState;
import com.template.states.JCTMasterState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JCTCreationFlow extends FlowLogic<Void> {
    private final String projectName;
    private final List<Party> contractors;
    private final List<Party> employers;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public JCTCreationFlow(String projectName, List<Party> contractors, List<Party> employers) {
        this.projectName = projectName;
        this.contractors = contractors;
        this.employers = employers;
    }

    private List<PublicKey> getOwningKeys(List<Party> parties) {
        List<PublicKey> keys = new ArrayList<PublicKey>();
        parties.forEach((party) -> keys.add(party.getOwningKey()));
        return keys;
    }

    @Override
    public ProgressTracker getProgressTracker() { return progressTracker; }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // Create the transaction components
        JCTMasterState outputState = new JCTMasterState(projectName, Arrays.asList(getOurIdentity()), contractors);

        //List of required signers:
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey());
        requiredSigners.addAll(getOwningKeys(contractors));

        Command createCommand = new Command<>(new JCTMasterContract.Create(), requiredSigners);

        // Create a transaction builder and add the components
        // Transaction builders take in 'notary' party as the parameter to
        // instantiate the txBuilder
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, JCTMasterContract.ID)
                .addCommand(createCommand);

        // Verifying the transaction.
        txBuilder.verify(getServiceHub());

        // Signing the transaction.
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Creating a session with the collection of other parties.
        CollectSignaturesFlow otherPartySession = initiateFlow(otherParty);

        // Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));

        // We finalise the transaction and then send it to the counterparty.
        subFlow(new FinalityFlow(signedTx, otherPartySession));

        return null;
    }
}
