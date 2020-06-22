package com.mobymagic.clairediary.ui.chatrooms.util

import java.text.DateFormatSymbols
import java.util.*

object Util {
    internal fun getDate(): String {
        val cal = Calendar.getInstance()
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthOfYear = getMonthForInt(cal.get(Calendar.MONTH))
        val year = cal.get(Calendar.YEAR)

        return "$monthOfYear $dayOfMonth, $year"
    }

    private fun getMonthForInt(num: Int): String {
        var month = "wrong"
        val dfs = DateFormatSymbols()
        val months = dfs.months
        if (num in 0..11) {
            month = months[num]
        }
        return month
    }

}
