package com.mobymagic.clairediary.util.filters

interface ResultFilter<T> {

    fun filter(t: List<T>): List<T>
}