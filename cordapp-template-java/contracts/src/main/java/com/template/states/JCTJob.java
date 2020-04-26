package com.template.states;

import net.corda.core.serialization.CordaSerializable;

import java.time.LocalDate;

// *******************************
// * JOB: A class that describes *
// *     a job in a schedule     *
// *******************************

@CordaSerializable
public class JCTJob {
    private String reference;
    private String description;
    private Double amount;
    private LocalDate expectedEndDate;
    private Double percentageComplete;
//    private Amount<Currency> requestedAmount;
//    private Amount<Currency> amountPaidOut;
//    private Amount<Currency> paidOutMinusRet;
    // TODO: Am I implementing this?...
    // List<SecureHash> documentsRequired = Arrays.asList();
    private JCTJobStatus status;

    public JCTJob(String reference,
                  String description,
                  Double amount,
                  LocalDate expectedEndDate,
                  Double percentageComplete, JCTJobStatus status) {
        this.reference = reference;
        this.description = description;
        this.amount = amount;
        this.expectedEndDate = expectedEndDate;
        this.percentageComplete = percentageComplete;
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }

    public Double getPercentageComplete() {
        return percentageComplete;
    }

//    public Double getRequestedAmount() {
//        return requestedAmount;
//    }
//
//    public Double getAmountPaidOut() {
//        return amountPaidOut;
//    }
//
//    public Double getPaidOutMinusRet() {
//        return paidOutMinusRet;
//    }

    public JCTJobStatus getStatus() {
        return status;
    }

    public class JCTJobBuilder {

        private String reference;
        private String description;
        private Double amount;
        private LocalDate expectedEndDate;
        private Double percentageComplete;
        private JCTJobStatus status;

        public JCTJobBuilder(JCTJob origin) {
            this.reference = origin.getReference();
            this.description = origin.getDescription();
            this.amount = origin.getAmount();
            this.expectedEndDate = origin.getExpectedEndDate();
            this.percentageComplete = origin.getPercentageComplete();
            this.status = origin.getStatus();
        }

        public JCTJobBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public JCTJobBuilder withAmount(Double amount) {
            this.amount = amount;
            return this;
        }

        public JCTJobBuilder withPercentage(Double percentageComplete) {
            this.percentageComplete = percentageComplete;
            return this;
        }

        public JCTJobBuilder withExpectedEndDate(LocalDate dueDate) {
            this.expectedEndDate = dueDate;
            return this;
        }

        public JCTJobBuilder withStatus(JCTJobStatus status) {
            this.status = status;
            return this;
        }

        public JCTJob build() {
            return new JCTJob(reference, description, amount, expectedEndDate, percentageComplete, status);
        }
    }

    public JCTJobBuilder copyBuilder() {
      return new JCTJobBuilder(this);
    }

    public boolean equals(Object obj) {
        boolean flag = false;
        if (obj instanceof JCTJob) {
            JCTJob job = (JCTJob) obj;
            if (job.getAmount().equals(this.getAmount()) &&
                job.getStatus().equals(this.getStatus()) &&
                job.getReference().equals(this.getReference()) &&
                job.getDescription().equals(this.getDescription()) &&
                job.getExpectedEndDate().equals(this.getExpectedEndDate()) &&
                job.getPercentageComplete().equals(this.getPercentageComplete())) {
                flag = true;
            }
        }
        return flag;
    }

    public boolean equalsExcept(Object obj, String check) {
        boolean flag = false;
        if (obj instanceof JCTJob) {
            JCTJob job = (JCTJob) obj;
            if (check.equals("Amount") &&
                job.getAmount().equals(this.getAmount()) &&
                job.getStatus().equals(this.getStatus()) &&
                job.getReference().equals(this.getReference()) &&
                job.getDescription().equals(this.getDescription()) &&
                job.getExpectedEndDate().equals(this.getExpectedEndDate()) &&
                job.getPercentageComplete().equals(this.getPercentageComplete())) {
                    flag = true;
            }
            if (check.equals("Status") &&
                    job.getAmount().equals(this.getAmount()) &&
                    job.getReference().equals(this.getReference()) &&
                    job.getDescription().equals(this.getDescription()) &&
                    job.getExpectedEndDate().equals(this.getExpectedEndDate()) &&
                    job.getPercentageComplete().equals(this.getPercentageComplete())) {
                flag = true;
            }
            if (check.equals("Reference") &&
                    job.getAmount().equals(this.getAmount()) &&
                    job.getStatus().equals(this.getStatus()) &&
                    job.getDescription().equals(this.getDescription()) &&
                    job.getExpectedEndDate().equals(this.getExpectedEndDate()) &&
                    job.getPercentageComplete().equals(this.getPercentageComplete())) {
                flag = true;
            }
            if (check.equals("Description") &&
                    job.getAmount().equals(this.getAmount()) &&
                    job.getStatus().equals(this.getStatus()) &&
                    job.getReference().equals(this.getReference()) &&
                    job.getExpectedEndDate().equals(this.getExpectedEndDate()) &&
                    job.getPercentageComplete().equals(this.getPercentageComplete())) {
                flag = true;
            }
            if (check.equals("Expected") &&
                    job.getAmount().equals(this.getAmount()) &&
                    job.getStatus().equals(this.getStatus()) &&
                    job.getReference().equals(this.getReference()) &&
                    job.getDescription().equals(this.getDescription()) &&
                    job.getPercentageComplete().equals(this.getPercentageComplete())) {
                flag = true;
            }
            if (check.equals("Percentage") &&
                    job.getAmount().equals(this.getAmount()) &&
                    job.getStatus().equals(this.getStatus()) &&
                    job.getReference().equals(this.getReference()) &&
                    job.getDescription().equals(this.getDescription()) &&
                    job.getExpectedEndDate().equals(this.getExpectedEndDate())) {
                flag = true;
            }
        }
        return flag;
    }
}