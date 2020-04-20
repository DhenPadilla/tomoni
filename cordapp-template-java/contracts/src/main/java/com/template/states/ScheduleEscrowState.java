package com.template.states;

import com.template.contracts.JCTContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// *********
// * State *
// *********
@BelongsToContract(JCTContract.class)
public class ScheduleEscrowState implements ContractState, LinearState {

    private Instant issuanceDate;
    private final UniqueIdentifier linearId;
    private final String projectName;
    private final List<Party> employers;
    private final List<Party> contractors;
    private Double contractSum;
    private final Double retentionPercentage;
    private Boolean allowAccountPayments = true;
    private Double netCumulateValue = 0.0;
    private Double previousCumulativeValue = 0.0;
    private List<JCTJob> jobs;

    @ConstructorForDeserialization
    public ScheduleEscrowState(UniqueIdentifier linearId, String projectName, List<Party> employer, List<Party> contractor, Double retentionPercentage) {
        this.linearId = linearId;
        this.projectName = projectName;
        this.employers = employer;
        this.contractors = contractor;
        this.retentionPercentage = retentionPercentage;
    }

    public ScheduleEscrowState(UniqueIdentifier linearId, String projectName, List<Party> employer, List<Party> contractor, Double retentionPercentage, List<JCTJob> jobs) {
        this.linearId = new UniqueIdentifier();
        this.projectName = projectName;
        this.employers = employer;
        this.contractors = contractor;
        this.retentionPercentage = retentionPercentage;
        this.jobs = jobs;
    }

    // JCT-based state
    public ScheduleEscrowState copyWithNewJobs(List<JCTJob> jobs) {
        return new ScheduleEscrowState(this.linearId,
                                       this.projectName,
                                       this.employers,
                                       this.contractors,
                                       this.retentionPercentage,
                                        jobs);
    }

//    public ScheduleEscrowState signDate(Instant issuanceDate) {
//        ScheduleEscrowState stateWithSignedDate =
//                new ScheduleEscrowState(this.linearId,
//                                        this.projectName,
//                                        this.employers,
//                                        this.contractors,
//                                        this.retentionPercentage,
//                                       null);
//        stateWithSignedDate.issuanceDate = issuanceDate;
//        return stateWithSignedDate;
//    }


    public String getProjectName() { return projectName; }

    public List<Party> getEmployers() {
        return employers;
    }

    public List<Party> getContractors() {
        return contractors;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        final List<AbstractParty> parts = new ArrayList<>();
        parts.addAll(employers);
        parts.addAll(contractors);
        return parts;
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return null;
    }

    public List<JCTJob> getJobs() { return jobs; }

    public Double getContractSum() {
        return contractSum;
    }
}