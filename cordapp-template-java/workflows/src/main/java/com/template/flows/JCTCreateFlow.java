//package com.template.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.template.contracts.JCTContract;
//import com.template.states.JCTState;
//import net.corda.core.contracts.Command;
//import net.corda.core.flows.*;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.transactions.TransactionBuilder;
//import net.corda.core.utilities.ProgressTracker;
//
//import java.security.PublicKey;
//import java.util.Arrays;
//import java.util.List;
//
//// ******************
//// * Initiator flow *
//// ******************
//@InitiatingFlow
//@StartableByRPC
//public class JCTCreateFlow extends FlowLogic<Void> {
//    private final String projectName;
//    private final String projectDetails;
//    private final Party otherParty;
//
//    /**
//     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
//     */
//    private final ProgressTracker progressTracker = new ProgressTracker();
//
//    public JCTCreateFlow(String projectName, String projectDetails, Party otherParty) {
//        this.projectName = projectName;
//        this.projectDetails = projectDetails;
//        this.otherParty = otherParty;
//    }
//
//    @Override
//    public ProgressTracker getProgressTracker() {
//        return progressTracker;
//    }
//
//    /**
//     * Any state's flow logic is encapsulated within the call() method.
//     */
//    @Suspendable
//    @Override
//    public Void call() throws FlowException {
//        // We retrieve the notary identity from the network map.
//        // The first thing we do in our flow is retrieve the a notary from the
//        // node’s ServiceHub.
//        // ServiceHub.networkMapCache provides information about the
//        // other nodes on the network and the services that they offer.
//        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//
//        // Create the transaction components
//        // (I think getOurIdentity() get's the Flow-caller's Party object
//        JCTState outputState = new JCTState(projectName, projectDetails, getOurIdentity(), otherParty);
//
//        //List of required signers:
//        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), otherParty.getOwningKey());
//
//        Command command = new Command<>(new JCTContract.Create(), getOurIdentity().getOwningKey());
//
//        // Create a transaction builder and add the components
//        // Transaction builders take in 'notary' party as the parameter to
//        // instantiate the txBuilder
//        TransactionBuilder txBuilder = new TransactionBuilder(notary)
//                .addOutputState(outputState, JCTContract.ID)
//                .addCommand(command);
//
//        // Verifying the transaction.
//        txBuilder.verify(getServiceHub());
//
//        // Signing the transaction.
//        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
//
//        // Creating a session with the other party.
//        FlowSession otherPartySession = initiateFlow(otherParty);
//
//        // Obtaining the counterparty's signature.
//        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
//                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));
//
//        // We finalise the transaction and then send it to the counterparty.
//        subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
//
//        return null;
//    }
//}