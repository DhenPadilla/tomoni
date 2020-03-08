package com.template.states.clauses;

import net.corda.core.serialization.CordaSerializable;

/**
 * Simple POJO class for the claim details.
 * Corda uses its own serialization framework hence the class needs to be annotated with @CordaSerializable, so that
 * the objects of the class can be serialized to be passed across different nodes.
 */
@CordaSerializable

public class JCTCondition {
    private final String conditionNumber;
    private final String conditionDescription;
    private final String conditionStatus;

    public JCTCondition(String conditionNumber, String conditionDescription, String conditionStatus) {
        this.conditionNumber = conditionNumber;
        this.conditionDescription = conditionDescription;
        this.conditionStatus = conditionStatus;
    }

    public String getConditionNumber() {
        return conditionNumber;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }
}

