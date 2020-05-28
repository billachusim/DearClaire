package com.mobymagic.clairediary.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import com.mobymagic.clairediary.R
import kotlinx.android.synthetic.main.layout_audio_download_progress.view.*

class AudioDownloadProgressView : RelativeLayout {

    var progressListener: ((downloading: Boolean) -> Unit)? = null
    private var downloading = false
    var clickListener: View.OnClickListener? = null
    @DrawableRes
    var mainIcon: Int = R.drawable.ic_round_play_arrow_white_24

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.layout_audio_download_progress, this)
        circularProgress.setOnClickListener {
            downloading = !downloading
            setProgressViews(downloading)
            progressListener?.invoke(downloading)
            clickListener?.onClick(it)
        }
    }

    //TODO any way to improve this?
    fun setProgress(progress: Float) {
        circularProgress.progress = progress
    }

    //TODO any way to improve this?
    fun getProgress(): Float {
        return circularProgress.progress
    }

    fun setDownloading(downloading: Boolean) {
        this.downloading = downloading
        setProgressViews(downloading)
    }

    private fun setProgressViews(downloading: Boolean) {
        val icon = if (downloading) R.drawable.ic_round_clear_white_24 else mainIcon
        progressImage.setImageResource(icon)
        circularProgress.progressStarted = downloading
    }

}