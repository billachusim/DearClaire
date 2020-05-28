package com.mobymagic.clairediary.di

import com.mobymagic.clairediary.repository.*
import org.koin.dsl.module.applicationContext

val repositoryModule = applicationContext {

    bean {
        AuthRepository(get(), get())
    }

    bean {
        CommentRepository(get(), get(), get(), get(), get())
    }

    bean {
        FileRepository(get(), get(), get(), get())
    }

    bean {
        SessionRepository(get(), get(), get(), get(), get(), get())
    }

    bean {
        UserRepository(get(), get(), get())
    }

    bean {
        ClaireVartarRepository(get(), get(), get())
    }

}