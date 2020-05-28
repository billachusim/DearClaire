//package com.mobymagic.clairediary.fcm
//
//import com.google.firebase.iid.FirebaseInstanceIdService
//import timber.log.Timber
//import com.google.firebase.iid.FirebaseInstanceId
//import com.mobymagic.clairediary.repository.UserRepository
//import org.koin.android.ext.android.inject
//
//class FcmInstanceIdService: FirebaseInstanceIdService() {
//
//    private val userRepository: UserRepository by inject()
//
//    override fun onTokenRefresh() {
//        val refreshedToken = FirebaseInstanceId.getInstance().token
//        Timber.d("On FCM token refreshed: %s", refreshedToken)
//
//        val loggedInUser = userRepository.getLoggedInUser()
//        // Check if user is logged in
//        if(loggedInUser != null) {
//            // Update the user FCM id to the refreshed token
//            loggedInUser.fcmId = refreshedToken
//            userRepository.setLoggedInUser(loggedInUser)
//        }
//    }
//
//}