package com.mobymagic.clairediary.util

infix fun <T> Collection<T>.sameContentWith(collection: Collection<T>?) =
        collection?.let { this.size == it.size && this.containsAll(it) }