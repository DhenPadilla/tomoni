package com.template.contracts.clauses;

import com.template.contracts.JCTRecital;
import net.corda.core.transactions.LedgerTransaction;

public class JCTSixthRecital extends JCTRecital {
    public JCTSixthRecital(String recitalDesc, String recitalStatus) {
        super(recitalDesc, recitalStatus, issuanceDate);
    }

    @Override
    protected void verifyCreate(LedgerTransaction tx) {

    }

    @Override
    protected void voteOnCondition(LedgerTransaction tx) {

    }
}
