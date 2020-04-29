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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// *********
// * State *
// *********
@BelongsToContract(ReportContract.class)
public class ReportState implements ContractState, LinearState {
    private final UniqueIdentifier linearId;
    private final String jctJobReference;
    private final Instant dateOfSurvey;
    private final LocalDate requestedCompletionDate;
    private final Double requestedContractSum;
    private final String reportBody;
    private final ReportStatus status;
    private final List<Party> reporters;

    @ConstructorForDeserialization
    public ReportState(UniqueIdentifier linearId, String jctJobReference, Instant dateOfSurvey, LocalDate requestedCompletionDate, Double requestedContractSum, String reportBody, ReportStatus status, List<Party> reporters) {
        this.linearId = linearId;
        this.jctJobReference = jctJobReference;
        this.dateOfSurvey = dateOfSurvey;
        this.requestedCompletionDate = requestedCompletionDate;
        this.requestedContractSum = requestedContractSum;
        this.reportBody = reportBody;
        this.status = status;
        this.reporters = reporters;
    }

    public ReportState(ReportStatus status, String jctJobReference, Instant dateOfSurvey, LocalDate requestedCompletionDate, Double requestedContractSum, String reportBody, List<Party> reporters) {
        this.requestedCompletionDate = requestedCompletionDate;
        this.requestedContractSum = requestedContractSum;
        this.linearId = new UniqueIdentifier();
        this.status = status;
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

    public class ReportBuilder {
        private UniqueIdentifier linearId;
        private String jctJobReference;
        private Instant dateOfSurvey;
        private LocalDate requestedCompletionDate;
        private Double requestedContractSum;
        private String reportBody;
        private ReportStatus status;
        private List<Party> reporters;

        public ReportBuilder(ReportState origin) {
            this.linearId = origin.getLinearId();
            this.jctJobReference = origin.getJctJobReference();
            this.dateOfSurvey = origin.getDateOfSurvey();
            this.requestedCompletionDate = origin.getRequestedCompletionDate();
            this.requestedContractSum = origin.getRequestedContractSum();
            this.reportBody = origin.getReportBody();
            this.status = origin.getStatus();
            this.reporters = origin.getReporters();
        }

        public ReportState.ReportBuilder withReference(String ref) {
            this.jctJobReference = ref;
            return this;
        }

        public ReportState.ReportBuilder withSurveyDate(Instant surveyDate) {
            this.dateOfSurvey = surveyDate;
            return this;
        }

        public ReportState.ReportBuilder withRequestedDate(LocalDate requestedCompletionDate) {
            this.requestedCompletionDate = requestedCompletionDate;
            return this;
        }

        public ReportState.ReportBuilder withRequestedSum(Double requestedSum) {
            this.requestedContractSum = requestedSum;
            return this;
        }

        public ReportState.ReportBuilder withBody(String body) {
            this.reportBody = body;
            return this;
        }

        public ReportState.ReportBuilder withStatus(ReportStatus status) {
            this.status = status;
            return this;
        }

        public ReportState build() {
            return new ReportState(this.linearId, this.jctJobReference, this.dateOfSurvey, this.requestedCompletionDate, this.requestedContractSum, this.reportBody, this.status, this.reporters);
        }
    }

    public ReportState.ReportBuilder copyBuilder() {
        return new ReportState.ReportBuilder(this);
    }

    public LocalDate getRequestedCompletionDate() { return requestedCompletionDate; }

    public Double getRequestedContractSum() { return requestedContractSum; }

    public String getJctJobReference() {
        return jctJobReference;
    }

    public Instant getDateOfSurvey() {
        return dateOfSurvey;
    }

    public String getReportBody() {
        return reportBody;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public List<Party> getReporters() { return reporters; }

    public boolean equals(Object obj) {
        // Omits unique Identifier
        boolean flag = false;
        if (obj instanceof ReportState) {
            ReportState report = (ReportState) obj;
            if (report.getStatus().equals(this.getStatus()) &&
                    report.getDateOfSurvey().equals(this.getDateOfSurvey()) &&
                    report.getJctJobReference().equals(this.getJctJobReference()) &&
                    report.getReporters().equals(this.getReporters()) &&
                    report.getReportBody().equals(this.getReportBody()) &&
                    Objects.equals(report.getRequestedCompletionDate(), this.getRequestedCompletionDate()) &&
                    Objects.equals(report.getRequestedContractSum(), this.getRequestedContractSum())) {
                            flag = true;
                    }
            }
        return flag;
    }
}