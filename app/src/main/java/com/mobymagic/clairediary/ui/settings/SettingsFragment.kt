package com.mobymagic.clairediary.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.jobs.RemindDailyJob
import com.mobymagic.clairediary.widgets.NumberPickerPreference
import io.multimoon.colorful.Colorful
import io.multimoon.colorful.ThemeColor


class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    var requiresAuthentication: Boolean = false
    var openToNewUsers: Boolean = true

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.app_settings)
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        when (key) {
            Constants.PREF_KEY_DAILY_REMINDER_ENABLED -> {
                val reminderEnabled = sp.getBoolean(key, true)
                findPreference<Preference>(Constants.PREF_KEY_DAILY_REMINDER_HOUR)!!.isEnabled = reminderEnabled
            }
            Constants.PREF_KEY_DAILY_REMINDER_HOUR -> {
                val reminderHour = sp.getInt(key, RemindDailyJob.DEFAULT_HOUR).toLong()
                RemindDailyJob.reschedule(requireContext(), reminderHour)
            }
            Constants.PREF_THEME -> {
                val themes = resources.getStringArray(R.array.settings_theme_names)
                val curTheme = sp.getString(key, themes[0])
                var primaryColor = ThemeColor.PINK
                var accentColor = ThemeColor.RED
                var darkTheme = false

                when (curTheme) {
                    themes[0] -> {
                        primaryColor = ThemeColor.PINK
                        accentColor = ThemeColor.RED
                    }
                    themes[1] -> {
                        primaryColor = ThemeColor.BLUE
                        accentColor = ThemeColor.PINK
                    }
                    themes[2] -> {
                        primaryColor = ThemeColor.PINK
                        accentColor = ThemeColor.RED
                    }
                    themes[3] -> {
                        primaryColor = ThemeColor.RED
                        accentColor = ThemeColor.PINK
                    }
                    themes[4] -> {
                        primaryColor = ThemeColor.BLUE_GREY
                        accentColor = ThemeColor.PINK
                    }
                    themes[5] -> {
                        primaryColor = ThemeColor.BLACK
                        accentColor = ThemeColor.PINK
                        darkTheme = true
                    }
                    themes[6] -> {
                        primaryColor = ThemeColor.RED
                        accentColor = ThemeColor.PINK
                    }
                }

                Colorful().edit()
                        .setPrimaryColor(primaryColor)
                        .setAccentColor(accentColor)
                        .setDarkTheme(darkTheme)
                        .setTranslucent(false)
                        .apply(requireContext()) {
                            activity?.recreate()
                        }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is NumberPickerPreference) {
            val fragment = NumberPickerPreference.NumberPickerPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey())
            fragment.setTargetFragment(this, 0)
            fragmentManager?.let {
                fragment.show(
                        it,
                        "android.support.v7.preference.PreferenceFragment.DIALOG"
                )
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }

    }
}
