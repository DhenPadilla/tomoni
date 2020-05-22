package com.template.states;

import com.template.contracts.ScheduleClauseContract;
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
import java.util.Objects;
import java.util.stream.Collectors;

// *********
// * State *
// *********
@BelongsToContract(ScheduleClauseContract.class)
public class ScheduleClauseState implements ContractState, LinearState {

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
    public ScheduleClauseState(UniqueIdentifier linearId, String projectName, List<Party> employers, List<Party> contractors, Double contractSum, Double retentionPercentage, List<JCTJob> jobs) {
        this.linearId = linearId;
        this.projectName = projectName;
        this.employers = employers;
        this.contractors = contractors;
        this.contractSum = contractSum;
        this.retentionPercentage = retentionPercentage;
        this.jobs = jobs;
    }

    public ScheduleClauseState(String projectName, List<Party> employers, List<Party> contractors, Double contractSum, Double retentionPercentage, List<JCTJob> jobs) {
        this.linearId = new UniqueIdentifier();
        this.projectName = projectName;
        this.employers = employers;
        this.contractors = contractors;
        this.contractSum = contractSum;
        this.retentionPercentage = retentionPercentage;
        this.jobs = jobs;
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

    public Double getRetentionPercentage() { return this.retentionPercentage; }

    public String getProjectName() { return this.projectName; }

    public List<Party> getEmployers() {
        return this.employers;
    }

    public List<Party> getContractors() {
        return this.contractors;
    }

    private List<Party> convertNullToEmpty(List<Party> nullable) {
        List<Party> parts;
        if (nullable == null) {
            parts = new ArrayList<>();
        }
        else {
            parts = nullable;
        }
        return parts;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        final List<AbstractParty> parts = new ArrayList<>();
        parts.addAll(convertNullToEmpty(employers));
        parts.addAll(convertNullToEmpty(contractors));
        return parts;
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    public List<JCTJob> getJobs() {
        return this.jobs;
    }

    public Double getContractSum() {
        return this.contractSum;
    }

    public class ScheduleEscrowStateBuilder {

        private Instant issuanceDate;
        private UniqueIdentifier linearId;
        private String projectName;
        private List<Party> employers;
        private List<Party> contractors;
        private Double contractSum;
        private Double retentionPercentage;
        private Boolean allowAccountPayments = true;
        private Double netCumulateValue = 0.0;
        private Double previousCumulativeValue = 0.0;
        private List<JCTJob> jobs;

        public ScheduleEscrowStateBuilder(ScheduleClauseState origin) {
            this.projectName = origin.getProjectName();
            this.linearId = origin.getLinearId();
            this.employers = origin.getEmployers();
            this.contractors = origin.getContractors();
            this.contractSum = origin.getContractSum();
            this.retentionPercentage = origin.getRetentionPercentage();
            this.jobs = origin.getJobs();
        }

        public ScheduleClauseState.ScheduleEscrowStateBuilder withEmployers(List<Party> employers) {
            this.employers = employers;
            return this;
        }

        public ScheduleClauseState.ScheduleEscrowStateBuilder withContractSum(Double contractSum) {
            this.contractSum = contractSum;
            return this;
        }

        public ScheduleClauseState.ScheduleEscrowStateBuilder withPercentage(Double retentionPercentage) {
            this.retentionPercentage = retentionPercentage;
            return this;
        }

        public ScheduleClauseState.ScheduleEscrowStateBuilder withJobs(List<JCTJob> jobs) {
            this.jobs = jobs;
            return this;
        }


        public ScheduleClauseState build() {
            return new ScheduleClauseState(this.linearId, this.projectName, this.employers, this.contractors, this.contractSum, this.retentionPercentage, this.jobs);
        }
    }

    public ScheduleClauseState.ScheduleEscrowStateBuilder copyBuilder() {
        return new ScheduleClauseState.ScheduleEscrowStateBuilder(this);
    }

    public boolean equals(Object obj) {
        // Omits unique Identifier
        boolean flag = false;
        if (obj instanceof ScheduleClauseState) {
            ScheduleClauseState schedule = (ScheduleClauseState) obj;
            if (Objects.equals(schedule.getContractSum(), this.getContractSum()) &&
                    Objects.equals(schedule.getRetentionPercentage(), this.getRetentionPercentage()) &&
                    Objects.equals(schedule.getParticipants(), this.getParticipants()) &&
                    Objects.equals(schedule.getProjectName(), this.getProjectName())
                    ) {
                flag = true;
            }
        }
        return flag;
    }
}