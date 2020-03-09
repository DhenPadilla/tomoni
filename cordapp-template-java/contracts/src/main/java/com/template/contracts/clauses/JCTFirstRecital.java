package com.template.contracts.clauses;

import com.template.contracts.JCTRecital;
import net.corda.core.transactions.LedgerTransaction;

import java.time.Instant;

public class JCTFirstRecital extends JCTRecital {
    public JCTFirstRecital(String recitalDesc, String recitalStatus, Instant issuanceDate) {
        super(recitalDesc, recitalStatus, issuanceDate);
    }

    @Override
    protected void verifyCreate(LedgerTransaction tx) {

    }

    @Override
    protected void voteOnCondition(LedgerTransaction tx) {

    }
}