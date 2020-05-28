package com.mobymagic.clairediary.vo

data class PaymentPlan(
        val text: String,
        val amount: Double
) {

    override fun toString() = text

}