package com.mobymagic.clairediary.util

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.flutterwave.raveandroid.RaveUiManager
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.vo.PaymentPlan
import com.mobymagic.clairediary.vo.User
import java.util.*

class PaymentUtil(context: Context) {

    private val paymentPlans: List<PaymentPlan> = listOf(
            PaymentPlan(context.getString(R.string.donate_amount_200_naira), 200.0),
            PaymentPlan(context.getString(R.string.donate_amount_500_naira), 500.0),
            PaymentPlan(context.getString(R.string.donate_amount_1000_naira), 1000.0),
            PaymentPlan(context.getString(R.string.donate_amount_2000_naira), 2000.0)
    )

    fun donate(activity: Activity, user: User, currency: String = "NGN") {
        val builderSingle = AlertDialog.Builder(activity)
        builderSingle.setTitle(R.string.donate_amount_page_title)

        val arrayAdapter = ArrayAdapter(
                activity,
                android.R.layout.select_dialog_singlechoice,
                paymentPlans
        )

        builderSingle.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }

        builderSingle.setAdapter(arrayAdapter) { dialog, which ->
            dialog.dismiss()
            val amount = arrayAdapter.getItem(which)?.amount
            if (amount != null) {
                makePayment(activity, user, amount, currency)
            }
        }
        builderSingle.show()
    }

    private fun makePayment(
            activity: Activity,
            user: User,
            amount: Double,
            currency: String = "NG"
    ) {
        val transactionRef = user.nickname + UUID.randomUUID().toString()
        RaveUiManager(activity)
                .setAmount(amount)
                .setCurrency(currency)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptUssdPayments(true)
                .acceptBankTransferPayments(true)
                .acceptSaBankPayments(true)
                .acceptUkPayments(true)
                .allowSaveCardFeature(true)
                .onStagingEnv(false)
                .withTheme(R.style.RaveTheme)
                .setNarration(activity.getString(R.string.app_name))
                .setTxRef(transactionRef)
                .setEmail(user.email)
                .setPublicKey(activity.getString(R.string.rave_public_key))
                .setEncryptionKey(activity.getString(R.string.rave_secret_key))
                .initialize()
    }

}