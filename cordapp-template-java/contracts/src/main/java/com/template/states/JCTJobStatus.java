package com.template.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum JCTJobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CONFIRMED,
    PAID,
    ON_ACCOUNT_PAYMENT
}
