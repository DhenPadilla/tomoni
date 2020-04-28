package com.template.states;

import com.template.contracts.ReportContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ReportContract.class)
public class ReportState implements ContractState, LinearState {
    private final UniqueIdentifier linearId;
    private final String jctJobReference;
    private final Instant dateOfSurvey;
    private final String reportBody;
    private final List<Party> reporters;

    @ConstructorForDeserialization
    public ReportState(UniqueIdentifier linearId, String jctJobReference, Instant dateOfSurvey, String reportBody, List<Party> reporters) {
        this.linearId = linearId;
        this.jctJobReference = jctJobReference;
        this.dateOfSurvey = dateOfSurvey;
        this.reportBody = reportBody;
        this.reporters = reporters;
    }

    public ReportState(String jctJobReference, Instant dateOfSurvey, String reportBody, List<Party> reporters) {
        this.linearId = new UniqueIdentifier();
        this.jctJobReference = jctJobReference;
        this.dateOfSurvey = dateOfSurvey;
        this.reportBody = reportBody;
        this.reporters = reporters;
    }

//    public ReportState copy() {
//
//    }


    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        final List<AbstractParty> parts = new ArrayList<>();
        parts.addAll(reporters);
        return parts;
    }

    private boolean isNullOrEmpty(String str) {
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }

    public boolean checkIfEmpty() {
        if (isNullOrEmpty(jctJobReference) ||
            isNullOrEmpty(reportBody) ||
            this.dateOfSurvey == null) {
            return true;
        }
        return false;
    }

    public String getJctJobReference() {
        return jctJobReference;
    }

    public Instant getDateOfSurvey() {
        return dateOfSurvey;
    }

    public String getReportBody() {
        return reportBody;
    }
}