package com.template.contracts.clauses;

import com.template.contracts.JCTRecital;
import net.corda.core.transactions.LedgerTransaction;

import java.time.Instant;

public class JCTSecondRecital extends JCTRecital {
    public JCTSecondRecital(String recitalDesc, String recitalStatus, Instant issuanceDate) {
        super(recitalDesc, recitalStatus, issuanceDate);
    }

    @Override
    protected void verifyCreate(LedgerTransaction tx) {

    }

    @Override
    protected void voteOnCondition(LedgerTransaction tx) {

    }
}
