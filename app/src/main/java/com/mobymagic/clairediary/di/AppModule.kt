package com.mobymagic.clairediary.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.mobymagic.clairediary.AppExecutors
import id.zelory.compressor.Compressor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val appModule = applicationContext {

    bean {
        AppExecutors()
    }

    bean {
        Gson()
    }

    bean {
        Compressor(androidApplication())
    }

    bean {
        FirebaseFirestore.getInstance()
    }

    bean {
        FirebaseAuth.getInstance()
    }

    bean {
        FirebaseStorage.getInstance().reference
    }

    bean {
        FirebaseStorage.getInstance()
    }

}