package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ScheduleClauseContract;
import com.template.states.JCTJob;
import com.template.states.ScheduleClauseState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agree the creation of a [JobState] representing a job organised by a developer and carried out by a [contractor].
 * The job is split into a set of [milestones].
 *
 * Should be run by the developer.
 *
 *  milestones the milestones involved in the job.
 *  contractors the contractor carrying out the job.
 *  notaryToUse the notary to assign the output state to.
 */

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class CreateJCTScheduleFlow extends FlowLogic<UniqueIdentifier> {
    private final String projectName;
    private final List<Party> contractors;
    private final List<Party> employers;
    private final Double contractAmount;
    private final Double retentionPercentage;
    private final Boolean allowPaymentOnAccount;
    private final List<JCTJob> jobs;
    private final Party notaryToUse;
    private final String jobReference;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public CreateJCTScheduleFlow(String projectName,
                                 List<Party> employers,
                                 List<Party> contractors,
                                 Double contractAmount,
                                 Double retentionPercentage,
                                 Boolean allowPaymentOnAccount,
                                 List<JCTJob> jobs,
                                 Party notaryToUse,
                                 String jobReference) {
        this.projectName = projectName;
        this.employers = employers;
        this.contractors = contractors;
        this.contractAmount = contractAmount;
        this.retentionPercentage = retentionPercentage;
        this.allowPaymentOnAccount = allowPaymentOnAccount;
        this.jobs = jobs;
        this.notaryToUse = notaryToUse;
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

    @Suspendable
    @Override
    public UniqueIdentifier call() throws FlowException {
    ScheduleClauseState outputScheduleState = new ScheduleClauseState(projectName, employers, contractors, contractAmount, retentionPercentage, jobs);
        List<PublicKey> requiredSigners = outputScheduleState.getParticipantKeys();
        Command createCommand = new Command<>(new ScheduleClauseContract.Commands.CreateSchedule(), requiredSigners);

        // Create a transaction builder and add the components
        // Transaction builders take in 'notary' party as the parameter to
        // instantiate the txBuilder
        TransactionBuilder txBuilder = new TransactionBuilder(notaryToUse)
                .addOutputState(outputScheduleState)
                .addCommand(createCommand);

        // Verifying the transaction.
        txBuilder.verify(getServiceHub());

        SignedTransaction partiallySignedTransaction = getServiceHub().signInitialTransaction(txBuilder);

        // Create list of parties and initiate flows.
        List<Party> participants = new ArrayList<>();
        participants.addAll(outputScheduleState.getContractors());
        participants.addAll(outputScheduleState.getEmployers());

        Set<FlowSession> sessions = participants.stream().map(it -> initiateFlow(it)).collect(Collectors.toSet());

        // CHECK IF THIS BLOCKS
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partiallySignedTransaction,
                sessions)); //, CollectSignaturesFlow.Companion.tracker()));

        // We finalise the transaction and then send it to the counterparties.
        subFlow(new FinalityFlow(fullySignedTx, sessions));


        return outputScheduleState.getLinearId();
    }
}


