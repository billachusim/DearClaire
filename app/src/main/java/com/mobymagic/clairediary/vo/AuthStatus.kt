package com.mobymagic.clairediary.vo

enum class AuthStatus {
    //the user is logged in and can access all features of the app
    LOGGED_IN,

    // the user is logged out can only access open features
    LOGGED_OUT,

    //the user is logged in but needs to enter secret code to access key features
    LOGGED_IN_LOCKED_OUT
}