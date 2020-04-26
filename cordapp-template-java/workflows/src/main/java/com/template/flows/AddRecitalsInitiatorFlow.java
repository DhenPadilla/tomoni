//package com.template.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.google.common.collect.ImmutableList;
//import com.template.contracts.JCTContract;
//import com.template.contracts.JCTRecital;
//import com.template.states.JCTState;
//import net.corda.core.contracts.Command;
//import net.corda.core.contracts.StateAndRef;
//import net.corda.core.contracts.UniqueIdentifier;
//import net.corda.core.flows.*;
////import com.template.flows.JCTBaseFlow;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.transactions.TransactionBuilder;
//import net.corda.core.utilities.ProgressTracker;
//
//import java.security.PublicKey;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//// ******************
//// * Initiator flow *
//// ******************
//@InitiatingFlow
//@StartableByRPC
//public class AddRecitalsInitiatorFlow extends JCTBaseFlow {
//    private final UniqueIdentifier linearId;
//    private final String projectName;
//    private final List<JCTRecital> recitals;
//    private final List<Party> employer;
//    private final List<Party> contractor;
//
//    /**
//     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
//     */
//    private final ProgressTracker progressTracker = new ProgressTracker();
//
//    public AddRecitalsInitiatorFlow(UniqueIdentifier linearId, String projectName, List<JCTRecital> recitals, List<Party> employer, List<Party> contractor) {
//        this.linearId = linearId;
//        this.projectName = projectName;
//        this.recitals = recitals;
//        this.employer = employer;
//        this.contractor = contractor;
//    }
//
//    @Override
//    public ProgressTracker getProgressTracker() {
//        return progressTracker;
//    }
//
//    @Suspendable
//    private JCTState createOutJCTWithRecitals(JCTState inputJCT) {
//        return inputJCT.appendRecitals(recitals);
//    }
//
//    /**
//     * Any state's flow logic is encapsulated within the call() method.
//     * @return
//     */
//    @Suspendable
//    @Override
//    public SignedTransaction call() throws FlowException {
//        // We retrieve the notary identity from the network map.
//        // The first thing we do in our flow is retrieve the a notary from the
//        // node’s ServiceHub.
//        // ServiceHub.networkMapCache provides information about the
//        // other nodes on the network and the services that they offer.
////        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        Party notary = getFirstNotary();
//
//        // Create the set of sessions with the contractor parties
//        Set<FlowSession> sessions = contractor.stream().map(it -> initiateFlow(it)).collect(Collectors.toSet());
//
//        // Get input states:
//        final StateAndRef<JCTState> JCTWithEmptyRecitals = getJCTStateByLinearId(linearId);
//        final JCTState inputJCT = JCTWithEmptyRecitals.getState().getData();
//
//        // Just a small check if this flow is initiated by a contractor:
//        if (inputJCT.getContractor().contains(getOurIdentity())) {
//            throw new IllegalStateException("Adding Recitals to a JCT can only be initiated by the Employer(s).");
//        }
//
//        // Create the transaction components
//        final JCTState outputState = createOutJCTWithRecitals(inputJCT);
//
//        // Check to see if any of the signers have changed:
//        if (!outputState.getParticipants().containsAll(inputJCT.getParticipants())) {
//            throw new IllegalStateException("JCT Participants (Employer(s)/Contractor(s)) must not change when appending recitals");
//        }
//
//        // Create the AddRecitals command.
//        final List<PublicKey> requiredSigners = new ImmutableList.Builder<PublicKey>()
//                        .addAll(outputState.getParticipantKeys()).build();
//        final Command addRecitalsCommand = new Command<>(new JCTContract.Commands.AddRecitals(), requiredSigners);
//
//        // Create a transaction builder and add the components
//        // Transaction builders take in 'notary' party as the parameter to
//        // instantiate the txBuilder
//        TransactionBuilder txBuilder = new TransactionBuilder(notary)
//                .addInputState(JCTWithEmptyRecitals)
//                .addOutputState(outputState, JCTContract.ID)
//                .addCommand(addRecitalsCommand);
//        // Verifying the transaction.
//        txBuilder.verify(getServiceHub());
//
//        // Signing the transaction.
//        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
//
//        // Obtaining the counterparty's signature.
////        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
////                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));
//        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx,
//                sessions, CollectSignaturesFlow.Companion.tracker()));
//
//        // We finalise the transaction and then send it to the counterparties.
//        return subFlow(new FinalityFlow(fullySignedTx, sessions));
//    }
//}
