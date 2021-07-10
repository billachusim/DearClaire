package com.mobymagic.clairediary.vo

/**
 * Class used by the splash screen to determine what page to go to,
 * Also used by other other components to determine where to go
 */
data class SplashResult(
    val userId: String? = null,
    val userType: User.UserType = User.UserType.REGULAR,
    val secretCode: String? = null,
    val action: SplashAction
) {

    enum class SplashAction {
        OPEN_ONBOARDING, OPEN_LOCK_SCREEN, OPEN_SESSIONS_HOME, OPEN_CREATE_PROFILE, OPEN_DESTINATION
    }

}