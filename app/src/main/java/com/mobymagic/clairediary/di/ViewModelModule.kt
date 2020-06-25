package com.mobymagic.clairediary.di

import com.mobymagic.clairediary.ui.alteregologin.AlterEgoLoginViewModel
import com.mobymagic.clairediary.ui.archivesessions.ArchiveSessionViewModel
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.commentlist.CommentListViewModel
import com.mobymagic.clairediary.ui.createprofile.CreateProfileViewModel
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarViewModel
import com.mobymagic.clairediary.ui.createsession.CreateSessionViewModel
import com.mobymagic.clairediary.ui.ego.EgoViewModel
import com.mobymagic.clairediary.ui.forgotsecretcode.ForgotSecretCodeViewModel
import com.mobymagic.clairediary.ui.guestego.GuestEgoViewModel
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.ui.sessionlist.SessionListViewModel
import com.mobymagic.clairediary.ui.signin.SignInViewModel
import com.mobymagic.clairediary.ui.signup.SignUpViewModel
import com.mobymagic.clairediary.ui.splash.SplashViewModel
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext

val viewModelModule = applicationContext {

    viewModel {
        CommentListViewModel(get())
    }

    viewModel {
        AlterEgoLoginViewModel(get(), get())
    }

    viewModel {
        CreateSessionViewModel(get(), get(), get(), get())
    }

    viewModel {
        SessionDetailViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ForgotSecretCodeViewModel(get())
    }

    viewModel {
        SessionListViewModel(get(), get(), get(), get())
    }

    viewModel {
        SignUpViewModel(get(), get())
    }

    viewModel {
        CreateProfileViewModel(get(), get())
    }

    viewModel {
        SignInViewModel(get(), get(), get())
    }

    viewModel {
        SplashViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AuthViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ClaireVartarViewModel(get())
    }

    viewModel {
        EgoViewModel(get(), get(), get(), get())
    }

    viewModel {
        ArchiveSessionViewModel(get())
    }

    viewModel {
        GuestEgoViewModel(get(), get(), get(), get())
    }
}