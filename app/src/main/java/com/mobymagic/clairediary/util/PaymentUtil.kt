package com.mobymagic.clairediary.util

import android.app.Activity
import com.flutterwave.raveandroid.RavePayManager
import com.mobymagic.clairediary.R
import timber.log.Timber
import java.util.*

class PaymentUtil {

    /**
     * Launches the Rave payment Activity where users can enter their card or bank details to make
     * payments.
     * @param activity An activity
     * @param email The user email address
     * @param amount The amount to charge the user
     * @param currency The currency to charge the user in. Default is Naira (NGN)
     * @param country A 2 letters representation of the user country. Default is Nigeria (NG)
     * @return a unique transaction reference for this request
     */
    fun startPaymentActivity(
            activity: Activity,
            email: String,
            amount: Double,
            currency: String = "NGN",
            country: String = "NG"
    ): String {
        Timber.d(
                "Starting payment activity. Email: %s, amount: %d, currency: %s, country: %s",
                email, amount, currency, country
        )
        val txRef = email + UUID.randomUUID().toString()
        Timber.d("Unique transaction reference: %s", txRef)

        RavePayManager(activity)
                .setAmount(amount)
                .setCurrency(currency)
                .setCountry(country)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .onStagingEnv(false)
                .withTheme(R.style.RaveTheme)
                .setNarration(activity.getString(R.string.app_name))
                .setTxRef(txRef)
                .setEmail(email)
                .setPublicKey(activity.getString(R.string.rave_public_key))
                .setEncryptionKey(activity.getString(R.string.rave_secret_key))
                .initialize()
        return txRef
    }

}