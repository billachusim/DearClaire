package com.mobymagic.clairediary.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import timber.log.Timber

class AuthRepository(
        private val androidUtil: AndroidUtil,
        private val firebaseAuth: FirebaseAuth
) {

    fun signUp(email: String, secretCode: String): LiveData<Resource<FirebaseUser>> {
        Timber.d("Signing up. Email: %s, Secret code: %s", email, secretCode)
        val signUpLiveData = MutableLiveData<Resource<FirebaseUser>>()

        // Set resource into loading state
        signUpLiveData.value = Resource.loading(androidUtil.getString(R.string.sign_up_signing_up))

        firebaseAuth.createUserWithEmailAndPassword(email, secretCode)
                .addOnSuccessListener { authResult ->
                    Timber.d("Sign up successful: %s", authResult)
                    signUpLiveData.value = Resource.success(authResult.user)
                }
                .addOnCanceledListener {
                    Timber.d("Sign up task cancelled")
                    signUpLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.sign_up_error_generic)
                    )
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    Timber.e(exception, "Error signing up")

                    when (exception) {
                        is FirebaseAuthWeakPasswordException ->
                            signUpLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_up_error_weak_secret_code)
                            )
                        is FirebaseAuthInvalidCredentialsException -> {
                            signUpLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_up_error_invalid_email)
                            )
                        }
                        is FirebaseAuthUserCollisionException -> {
                            signUpLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_up_error_user_exists)
                            )
                        }
                        else -> {
                            signUpLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_up_error_generic)
                            )
                        }
                    }
                }

        return signUpLiveData
    }

    fun signIn(email: String, secretCode: String): LiveData<Resource<FirebaseUser>> {
        Timber.d("Signing in. Email: %s, Secret code: %s", email, secretCode)
        val signInLiveData = MutableLiveData<Resource<FirebaseUser>>()

        // Set resource into loading state
        signInLiveData.value = Resource.loading(androidUtil.getString(R.string.sign_in_signing_in))

        firebaseAuth.signInWithEmailAndPassword(email, secretCode)
                .addOnSuccessListener { authResult ->
                    Timber.d("Sign in successful: %s", authResult)
                    signInLiveData.value = Resource.success(authResult.user)
                }
                .addOnCanceledListener {
                    Timber.d("Sign in task cancelled")
                    signInLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.sign_in_error_generic)
                    )
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error signing in")

                    when (exception) {
                        is FirebaseAuthInvalidUserException ->
                            signInLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_in_error_invalid_email)
                            )
                        is FirebaseAuthInvalidCredentialsException -> {
                            signInLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_in_error_invalid_secret_code)
                            )
                        }
                        else -> {
                            signInLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.sign_in_error_generic)
                            )
                        }
                    }
                }

        return signInLiveData
    }

    fun sendSecretCodeResetEmail(email: String): LiveData<Resource<AsyncRequest>> {
        Timber.d("Sending secret code reset to email: %s", email)
        val resetLiveData = MutableLiveData<Resource<AsyncRequest>>()

        // Set resource into loading state
        resetLiveData.value =
                Resource.loading(androidUtil.getString(R.string.forgot_secret_code_loading))

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Timber.d("Secret code reset email sent")
                    resetLiveData.value = Resource.success(AsyncRequest())
                }
                .addOnCanceledListener {
                    Timber.d("Secret code reset email task cancelled")
                    resetLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.forgot_secret_code_error_generic)
                    )
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    Timber.e(exception, "Error sending secret code reset email")

                    when (exception) {
                        is FirebaseAuthEmailException ->
                            resetLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.forgot_secret_code_error_invalid_email)
                            )
                        else -> {
                            resetLiveData.value = Resource.error(
                                    androidUtil
                                            .getString(R.string.forgot_secret_code_error_generic)
                            )
                        }
                    }
                }

        return resetLiveData
    }

}