package com.mobymagic.clairediary.ui.common

import com.mobymagic.clairediary.util.FragmentUtils

interface NavController {

    fun navigate(
            destination: NavFragment,
            addToBackStack: Boolean = false,
            onlyAddIfNotExist: Boolean = false,
            fragmentAnimation: FragmentUtils.FragmentAnimation = FragmentUtils.FragmentAnimation()
    )

    fun navigateTo(destination: NavFragment)

    fun remove(navFragment: NavFragment)
    fun removeFromBackstack(destination: NavFragment)

    fun clearBackStack()

    fun navigateToWithAuth(destination: NavFragment)

}