package com.template.states

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class JCTScheduleEscrowState(
        val employers: List<Party>,
        val contractors: List<Party>,
        val contractSum: Double,
        val retentionPercentage: Double,
        val allowPaymentOnAccount: Boolean,
        /** Which variables are constant and which change over time/event*/
        val grossCumulativeAmount: Double = 0.0, //total amount of money we valued so far for completed milestones or milestones with payment on accounts
        val retentionAmount: Double = 0.0, //amount retained so far
        val netCumulativeValue: Double = 0.0, // grossCumulativeAmount minus retentionAmount
        val previousCumulativeValue: Double = 0.0, // netCumulativeValue (previous) - netCumulativeValue (current) (Valuation)
        val jobs: List<JCTJob>,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    init {
        if (jobs.map { it.amount.token }.toSet().size != 1) {
            throw IllegalArgumentException("All milestones must be budgeted in the same currency.")
        }
    }

    override val participants = employers + contractors
}