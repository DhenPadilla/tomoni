package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.JCTMasterContract;
import com.template.states.JCTMasterState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.*;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class JCTCreationFlow extends FlowLogic<Void> {
    private final String projectName;
    private final Party contractor;
    private final Party employer;

    public JCTCreationFlow(String projectName, Party contractor, Party employer) {
        this.projectName = projectName;
        this.contractor = contractor;
        this.employer = employer;
    }

    //Progress tracker
    private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new IOU.");
    private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
    private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
    private final Step GATHERING_SIGS = new Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };

    private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // Stage 1.
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // Create the transaction components
        JCTMasterState outputState = new JCTMasterState(projectName, getOurIdentity(), contractor);
        // Generate unsigned transaction
        Party me = getOurIdentity();
        List<PublicKey> requiredSigners = Arrays.asList(me.getOwningKey());
        requiredSigners.add(contractor.getOwningKey());
//        Command createCommand = new Command<>(new JCTMasterContract.Create(), requiredSigners);
        Command createCommand = new Command<>(new JCTMasterContract.Create(), me.getOwningKey());

        // Create a transaction builder and add the components
        // Transaction builders take in 'notary' party as the parameter to
        // instantiate the txBuilder
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, JCTMasterContract.ID)
                .addCommand(createCommand);

        // Stage 2.
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // Verifying the transaction.
        txBuilder.verify(getServiceHub());


        // Stage 3.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        // Signing the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Stage 4.
        progressTracker.setCurrentStep(GATHERING_SIGS);
        // Create and collect set of flow sessions between
//        Set<FlowSession> sessions = contractor.stream().map(it -> initiateFlow(it)).collect(Collectors.toSet());
        FlowSession session = initiateFlow(contractor);
        // Obtain the counterparty signatures
//        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partSignedTx,
//                sessions, CollectSignaturesFlow.Companion.tracker()));
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                partSignedTx, Arrays.asList(session), CollectSignaturesFlow.Companion.tracker()));

        // Stage 5.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        // We finalise the transaction and then send it to the counterparty.
        subFlow(new FinalityFlow(partSignedTx, session));

        return null;
    }
}
