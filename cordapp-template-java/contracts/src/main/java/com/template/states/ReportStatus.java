package com.template.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum ReportStatus {
    ISSUED,
    PROCESSED,
    CONSUMED
}
