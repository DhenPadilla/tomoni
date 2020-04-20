//package com.template.states;
//
//import net.corda.core.contracts.*;
//import net.corda.core.serialization.CordaSerializable;
//
//import java.time.LocalDate;
//import java.util.Currency;
//
//// *******************************
//// * JOB: A class that describes *
//// *     a job in a schedule     *
//// *******************************
//
//@CordaSerializable
//public class JCTJob {
//    private String reference;
//    private String description;
//    private Amount<Currency> amount;
//    private LocalDate expectedEndDate;
//    private Double percentageComplete = 0.0;
//    private Amount<Currency> requestedAmount;
//    private Amount<Currency> amountPaidOut;
//    private Amount<Currency> paidOutMinusRet;
//    // TODO: Am I implementing this?...
//    // List<SecureHash> documentsRequired = Arrays.asList();
//    private JCTJobStatus status;
//
//    public JCTJob(String reference,
//                  String description,
//                  Amount<Currency> amount,
//                  LocalDate expectedEndDate,
//                  Amount<Currency> requestedAmount, Amount<Currency> amountPaidOut, Amount<Currency> paidOutMinusRet) {
//        this.reference = reference;
//        this.description = description;
//        this.amount = amount;
//        this.expectedEndDate = expectedEndDate;
//        this.requestedAmount = requestedAmount;
//        this.amountPaidOut = amountPaidOut;
//        this.paidOutMinusRet = paidOutMinusRet;
//        this.status = JCTJobStatus.PENDING;
//    }
//
//    public String getReference() {
//        return reference;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public Amount<Currency> getAmount() {
//        return amount;
//    }
//
//    public LocalDate getExpectedEndDate() {
//        return expectedEndDate;
//    }
//
//    public Double getPercentageComplete() {
//        return percentageComplete;
//    }
//
//    public Amount<Currency> getRequestedAmount() {
//        return requestedAmount;
//    }
//
//    public Amount<Currency> getAmountPaidOut() {
//        return amountPaidOut;
//    }
//
//    public Amount<Currency> getPaidOutMinusRet() {
//        return paidOutMinusRet;
//    }
//
//    public JCTJobStatus getStatus() {
//        return status;
//    }
//}