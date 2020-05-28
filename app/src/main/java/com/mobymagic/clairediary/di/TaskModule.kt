package com.mobymagic.clairediary.di

import com.mobymagic.clairediary.tasks.CreateProfileTask
import com.mobymagic.clairediary.tasks.SignUpTask
import org.koin.dsl.module.applicationContext

val taskModule = applicationContext {

    bean {
        SignUpTask(get(), get())
    }

    bean {
        CreateProfileTask(get(), get())
    }

}