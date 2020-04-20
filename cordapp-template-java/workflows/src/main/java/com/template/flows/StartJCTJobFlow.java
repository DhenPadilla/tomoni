package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.template.contracts.JCTContract;
import com.template.contracts.ScheduleEscrowContract;
import com.template.states.JCTJob;
import com.template.states.JCTJobStatus;
import com.template.states.JCTState;
import com.template.states.ScheduleEscrowState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class StartJCTJobFlow extends FlowLogic<UniqueIdentifier> {
    private final UniqueIdentifier linearId;
    private final String jobReference;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public StartJCTJobFlow(UniqueIdentifier linearId, String jobReference) {
        this.linearId = linearId;
        this.jobReference = jobReference;
    }

    private List<PublicKey> getOwningKeys(List<Party> parties) {
        List<PublicKey> keys = new ArrayList<PublicKey>();
        parties.forEach((party) -> keys.add(party.getOwningKey()));
        return keys;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    private Integer findJob(List<JCTJob> jobs, String reference) {
        List<JCTJob> result =  jobs.stream().filter(jctJob -> jctJob.getReference() == reference).collect(Collectors.toList());;

        if(result.isEmpty()){
            throw new IllegalStateException("Cannot find any JCTJob with reference [" + reference + "]");
        }

        return jobs.indexOf(result.get(0));
    }

    StateAndRef<ScheduleEscrowState> getScheduleEscrowStateByLinearId(UniqueIdentifier linearId) throws FlowException {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.UNCONSUMED,
                null);

        List<StateAndRef<ScheduleEscrowState>> obligations = getServiceHub().getVaultService().queryBy(ScheduleEscrowState.class, queryCriteria).getStates();
        if (obligations.size() != 1) {
            throw new FlowException(String.format("ScheduleEscrowState with id %s not found.", linearId));
        }
        return obligations.get(0);
    }

    @Suspendable
    @Override
    public UniqueIdentifier call() throws FlowException {
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // Get input states:
        final StateAndRef<ScheduleEscrowState> ScheduleEscrowWithPendingJobs = getScheduleEscrowStateByLinearId(linearId);
        final ScheduleEscrowState inputState = ScheduleEscrowWithPendingJobs.getState().getData();

        // Just a small check if this flow is initiated by a contractor:
        if (inputState.getContractors().contains(getOurIdentity())) {
            throw new IllegalStateException("Starting a Job within the Schedule can only be initiated by the Contractor(s).");
        }

        List<JCTJob> updatedJobs = inputState.getJobs();

        Integer jobIndex = findJob(updatedJobs, jobReference);

        updatedJobs.set(jobIndex, updatedJobs.get(jobIndex).copy().status(JCTJobStatus.IN_PROGRESS).build());

        ScheduleEscrowState outputState = inputState.copyWithNewJobs(updatedJobs);
        List<PublicKey> signers = inputState.getParticipantKeys();
        Command command = new Command<>(new ScheduleEscrowContract.Commands.StartJob(jobIndex), signers);

        // Create a transaction builder and add the components
        // Transaction builders take in 'notary' party as the parameter to
        // instantiate the txBuilder
        TransactionBuilder txBuilder = new TransactionBuilder(ScheduleEscrowWithPendingJobs.getState().getNotary())
                .addInputState(ScheduleEscrowWithPendingJobs)
                .addOutputState(outputState, ScheduleEscrowContract.ID)
                .addCommand(command);

        // Verifying the transaction.
        txBuilder.verify(getServiceHub());

        SignedTransaction partiallySignedTransaction = getServiceHub().signInitialTransaction(txBuilder);

        // Create list of parties and initiate flows.
        List<Party> participants = new ArrayList<>();
        participants.addAll(outputState.getContractors());
        participants.addAll(outputState.getEmployers());

        Set<FlowSession> sessions = participants.stream().map(it -> initiateFlow(it)).collect(Collectors.toSet());

        // CHECK IF THIS BLOCKS
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partiallySignedTransaction,
                sessions)); //, CollectSignaturesFlow.Companion.tracker()));

        // We finalise the transaction and then send it to the counterparties.
        subFlow(new FinalityFlow(fullySignedTx, sessions));


        return outputState.getLinearId();
    }

}
