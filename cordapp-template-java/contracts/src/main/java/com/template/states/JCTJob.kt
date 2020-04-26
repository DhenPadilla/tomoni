//package com.template.states
//
//import net.corda.core.serialization.CordaSerializable
//import java.time.LocalDate
//
//@CordaSerializable
//data class JCTJob(
//        /** Which variables are constant and which change over time/event*/
//        val reference: String,
//        val description: String,
//        val amount: Double, //milestone value
//        val expectedEndDate: LocalDate,
//        val percentageComplete: Double = 0.0,
////        val requestedAmount: Amount<Currency> = 0, //amount as per invoice/payment application from the contractor
////        val paymentOnAccount: Amount<Currency> = 0, //how much payment on account has been paid out (payment valuation)
////        val netMilestonePayment: Amount<Currency> = 0.POUNDS, //calculated based on milestone amount/payment on account less retention percentage
////        val documentsRequired : List<SecureHash> = listOf<SecureHash>(),
//        val status: JCTJobStatus = JCTJobStatus.PENDING) {
//
//    class Builder(origin: JCTJob) {
//        private var reference: String = origin.reference
//        private var description: String = origin.description
//        private var amount: Double = origin.amount
//        private var expectedEndDate: LocalDate = origin.expectedEndDate
//        private var percentageComplete: Double = origin.percentageComplete
//        //        val requestedAmount: Amount<Currency> = 0, //amount as per invoice/payment application from the contractor
////        val paymentOnAccount: Amount<Currency> = 0, //how much payment on account has been paid out (payment valuation)
////        val netMilestonePayment: Amount<Currency> = 0.POUNDS, //calculated based on milestone amount/payment on account less retention percentage
////        val documentsRequired : List<SecureHash> = listOf<SecureHash>(),
//        private var status: JCTJobStatus = origin.status
//
//        // also performs operations on 'this' and returns 'this'
//        fun reference(value: String) = this.also { reference = value }
//        fun description(value: String) = this.also { description = value }
//        fun amount(value: Double) = this.also { amount = value }
//        fun expectedEndDate(value: LocalDate) = this.also { expectedEndDate = value }
//        fun percentageComplete(value: Double) = this.also { percentageComplete = value }
//        fun status(value: JCTJobStatus) = this.also { status = value }
//
//        fun build() = JCTJob(reference, description, amount, expectedEndDate, percentageComplete, status)
//    }
//
//    fun copy() = Builder(this)
//}