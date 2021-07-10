package com.mobymagic.clairediary.util

import android.view.View
import androidx.annotation.AnimRes
import timber.log.Timber

class FragmentUtils {

    /**
     * Add the fragment if it doesn't already exists in the FragmentManager
     * @param fm The FragmentManager
     * @param containerRes The container to add the fragment to
     * @param fragment The fragment to add
     * @param tag Tag for fragment
     * param enterAnim The enter fragment transition
     * param exitAnim The exit fragment transition
     */
    fun addIfNotExist(
        fm: androidx.fragment.app.FragmentManager,
        containerRes: Int,
        fragment: androidx.fragment.app.Fragment,
        tag: String = fragment.javaClass.simpleName
    ) {
        val existingFragmentWithTag = fm.findFragmentByTag(tag)

        if (existingFragmentWithTag == null) {
            fm.beginTransaction()
                .add(containerRes, fragment, tag).commit()
        }
    }

    /**
     * Add the fragment if it doesn't already exists in the FragmentManager
     * @param fm The FragmentManager
     * @param containerRes The container to add the fragment to
     * @param fragment The fragment to add
     * @param addToBackStack If the transaction should be added to the back stack
     * @param onlyAddIfNotExist Indicates whether the fragment can be added if it exists already.
     * True if the fragment shouldn't be added when it already exists, false otherwise
     * @param fragmentAnimation The animation to use for the navigation
     */
    fun replace(
        fm: androidx.fragment.app.FragmentManager,
        containerRes: Int,
        fragment: androidx.fragment.app.Fragment,
        addToBackStack: Boolean = false,
        onlyAddIfNotExist: Boolean = false,
        fragmentAnimation: FragmentAnimation = FragmentAnimation()
    ) {
        val tag = fragment.javaClass.simpleName
        if (onlyAddIfNotExist) {
            val existingFragmentWithTag = fm.findFragmentByTag(tag)
            if (existingFragmentWithTag != null) {
                Timber.i("Fragment already exists, not adding")
                return
            }
        }

        val fragTransaction = fm.beginTransaction()
        fragmentAnimation.setupAnimation(fragTransaction)
        fragTransaction.replace(containerRes, fragment, tag)

        if (addToBackStack) {
            fragTransaction.addToBackStack(null)
        }

        Timber.d("Committing transaction")
        fragTransaction.commitAllowingStateLoss()
    }

    /**
     * Add the fragment if it doesn't already exists in the FragmentManager
     * @param fm The FragmentManager
     * @param containerRes The container to add the fragment to
     * @param fragment The fragment to add
     * @param addToBackStack If the transaction should be added to the back stack
     * @param onlyAddIfNotExist Indicates whether the fragment can be added if it exists already.
     * True if the fragment shouldn't be added when it already exists, false otherwise
     * @param fragmentAnimation The animation to use for the navigation
     */
    fun add(
        fm: androidx.fragment.app.FragmentManager,
        containerRes: Int,
        fragment: androidx.fragment.app.Fragment,
        addToBackStack: Boolean = false,
        onlyAddIfNotExist: Boolean = false,
        fragmentAnimation: FragmentAnimation = FragmentAnimation()
    ) {
        val tag = fragment.javaClass.simpleName
        if (onlyAddIfNotExist) {
            val existingFragmentWithTag = fm.findFragmentByTag(tag)
            if (existingFragmentWithTag != null) {
                Timber.i("Fragment already exists, not adding")
                return
            }
        }

        val fragTransaction = fm.beginTransaction()
        fragmentAnimation.setupAnimation(fragTransaction)
        fragTransaction.add(containerRes, fragment, tag)

        if (addToBackStack) {
            fragTransaction.addToBackStack(null)
        }

        Timber.d("Committing transaction")
        fragTransaction.commitAllowingStateLoss()
    }

    data class FragmentAnimation(
        @AnimRes private val enterAnim: Int = android.R.anim.fade_in,
        @AnimRes private val exitAnim: Int = android.R.anim.fade_out,
        @AnimRes private val popEnter: Int = android.R.anim.fade_in,
        @AnimRes private val popExit: Int = android.R.anim.fade_out,
        private val sharedElements: List<Pair<View, String>>? = null
    ) {

        fun setupAnimation(fragTransaction: androidx.fragment.app.FragmentTransaction) {
            if (sharedElements == null) {
                fragTransaction.setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
            } else {
                try {
                    for (sharedElement in sharedElements) {
                        fragTransaction.addSharedElement(sharedElement.first, sharedElement.second)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error with fragment animation: %s", this)
                }
            }
        }

    }

}