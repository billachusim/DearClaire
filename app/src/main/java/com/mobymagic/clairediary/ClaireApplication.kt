package com.mobymagic.clairediary

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.mobymagic.clairediary.Constants.PREF_KEY_DAILY_REMINDER_HOUR
import com.mobymagic.clairediary.di.*
import com.mobymagic.clairediary.jobs.AppJobCreator
import com.mobymagic.clairediary.jobs.RemindDailyJob
import com.mobymagic.clairediary.jobs.SyncProfileJob
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.*
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin

/**
 * App application class
 */
class ClaireApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(
            applicationContext,
            ImagePipelineConfig.newBuilder(applicationContext)
                .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                .experiment().setNativeCodeDisabled(true).build()
        )
        startKoin(
            this, listOf(
                apiModule, appModule, repositoryModule, utilModule, taskModule,
                viewModelModule
            )
        )

        initAppComponents(BuildConfig.DEBUG)
    }

    private fun initAppComponents(isDebugMode: Boolean) {
        val timberUtils: TimberUtils by inject()
        val strictUtils: StrictModeUtils by inject()
        val leakCanaryUtils: LeakCanaryUtils by inject()
        val notificationUtil: NotificationUtil by inject()
        val prefUtil: PrefUtil by inject()
        val appExecutors: AppExecutors by inject()
        val userRepository: UserRepository by inject()
        val jobManager = JobManager.create(this)

        if (leakCanaryUtils.isInLeakCanaryProcess()) {
            // We do not want to initialize components in leak canary process
            return
        }

        timberUtils.initTimber(isDebugMode)
        strictUtils.initStrictMode(isDebugMode)
        leakCanaryUtils.initLeakCanary()
        initEmojiLib()
        notificationUtil.createNotificationChannels()

        initTheme()
        val appJobCreator = AppJobCreator(appExecutors, prefUtil, userRepository, notificationUtil)
        val dailyReminderHour =
            prefUtil.getInt(PREF_KEY_DAILY_REMINDER_HOUR, RemindDailyJob.DEFAULT_HOUR).toLong()
        initJobs(jobManager, appJobCreator, dailyReminderHour)
    }

    private fun initJobs(jobManager: JobManager, appJobCreator: AppJobCreator, dailyHour: Long) {
        jobManager.addJobCreator(appJobCreator)
        RemindDailyJob.schedule(this, dailyHour)
        SyncProfileJob.runNow()
    }

    private fun initTheme() {
        val defaults = Defaults(
            primaryColor = ThemeColor.PINK,
            accentColor = ThemeColor.RED,
            useDarkTheme = false,
            translucent = false
        )
        initColorful(this, defaults)
    }

    private fun initEmojiLib() {
        EmojiManager.install(GoogleEmojiProvider())
    }

}