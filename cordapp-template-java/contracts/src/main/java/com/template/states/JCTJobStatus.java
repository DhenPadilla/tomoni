package com.template.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum JCTJobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ACCEPTED,
    PAID,
    ON_ACCOUNT_PAYMENT
}
