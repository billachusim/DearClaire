package com.mobymagic.clairediary.di

import com.mobymagic.clairediary.util.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val utilModule = applicationContext {

    bean {
        AndroidUtil(androidApplication())
    }

    bean {
        FontFactory(androidApplication())
    }

    bean {
        ThemeUtil(androidApplication())
    }

    bean {
        DonateUtil(androidApplication())
    }

    bean {
        InputUtil(androidApplication())
    }

    bean {
        PrefUtil(androidApplication())
    }

    bean {
        TimberUtils()
    }

    bean {
        AudioUtil(androidApplication(), get())
    }

    bean {
        ExoPlayerUtil(androidApplication())
    }

    bean {
        StrictModeUtils()
    }

    bean {
        LeakCanaryUtils(androidApplication())
    }

    bean {
        FragmentUtils()
    }

    bean {
        WindowUtils()
    }

    bean {
        PaymentUtil()
    }

    bean {
        NotificationUtil(androidApplication())
    }

}