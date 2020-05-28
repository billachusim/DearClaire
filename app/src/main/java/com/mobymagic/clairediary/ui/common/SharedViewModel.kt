package com.mobymagic.clairediary.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * common view model for sharing information between activity and fragments
 */
class SharedViewModel : ViewModel() {
    private val numberOfComments = MutableLiveData<Int>()

    fun setNumberOfComments(numberOfComments: Int?) {
        this.numberOfComments.value = numberOfComments
    }

    fun getNumberOfComments(): LiveData<Int> {
        return numberOfComments
    }
}