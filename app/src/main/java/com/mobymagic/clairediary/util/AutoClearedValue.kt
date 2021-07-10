package com.mobymagic.clairediary.util

import androidx.lifecycle.LifecycleObserver
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A lazy property that gets cleaned up when the fragment is destroyed.
 *
 * Accessing this variable in a destroyed fragment will throw NPE.
 */
class AutoClearedValue<T : Any>(val fragment: androidx.fragment.app.Fragment) :
    ReadWriteProperty<androidx.fragment.app.Fragment, T> {

    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : LifecycleObserver {
        })
    }

    override fun getValue(thisRef: androidx.fragment.app.Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(
        thisRef: androidx.fragment.app.Fragment,
        property: KProperty<*>,
        value: T
    ) {
        _value = value
    }
}

/**
 * Creates an [AutoClearedValue] associated with this fragment.
 */
fun <T : Any> androidx.fragment.app.Fragment.autoCleared() = AutoClearedValue<T>(this)