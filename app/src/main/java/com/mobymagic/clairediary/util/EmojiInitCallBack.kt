package com.mobymagic.clairediary.util

import android.util.Log
import androidx.emoji.text.EmojiCompat

class EmojiInitCallBack : EmojiCompat.InitCallback() {
    override fun onInitialized() {
        Log.i("EMOJICALLBACK", "EmojiCompat initialized")
    }

    override fun onFailed(throwable: Throwable?) {
        Log.e("EMOJICALLBACK", "EmojiCompat initialization failed", throwable)
    }
}