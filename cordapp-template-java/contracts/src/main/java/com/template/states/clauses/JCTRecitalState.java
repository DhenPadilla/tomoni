package com.template.states.clauses;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// ***************
// * State Model *
// ***************
@CordaSerializable
public class JCTRecitalState {

    private final String recitalDesc;
    private final String recitalStatus;
    private final Instant issuanceDate;

    public JCTRecitalState(String recitalDesc, String recitalStatus, Instant issuanceDate) {
        this.recitalDesc = recitalDesc;
        this.recitalStatus = recitalStatus;
        this.issuanceDate = issuanceDate;
    }

    public String getRecitalDesc() {
        return recitalDesc;
    }

    public String getRecitalStatus() {
        return recitalStatus;
    }

    public Instant getIssuanceDate() {
        return issuanceDate;
    }
}