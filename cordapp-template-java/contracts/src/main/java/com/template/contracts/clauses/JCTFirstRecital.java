package com.template.contracts.clauses;

import com.template.contracts.JCTRecital;
import net.corda.core.transactions.LedgerTransaction;

public class JCTFirstRecital extends JCTRecital {
    final private Integer recitalIndex;
    final private String recital;
    final private String recitalDetails;

    public JCTFirstRecital(Integer recitalIndex, String recital, String recitalDetails) {
        this.recitalIndex = recitalIndex;
        this.recital = recital;
        this.recitalDetails = recitalDetails;
    }

    public String getRecitalDetails() {
        return recitalDetails;
    }

    public static JCTFirstRecital createWithNoDetails(Integer recitalIndex, String recital) {
        return new JCTFirstRecital(recitalIndex, recital, "");
    }

    @Override
    protected void verifyCreate(LedgerTransaction tx) {

    }

    @Override
    protected void voteOnCondition(LedgerTransaction tx) {

    }
}