package com.template.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum JCTJobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CONFIRMED,
    DATE_AMENDMENT_REQUESTED,
    AMOUNT_AMENDMENT_REQUESTED,
    PAID,
    ON_ACCOUNT_PAYMENT
}
