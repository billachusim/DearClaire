package com.mobymagic.clairediary.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.StringRes
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.storage.FirebaseStorage
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.audio.AudioPlayerFragment
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference

private const val SEEK_BAR_RUNNABLE_DELAY = 1000L

class AudioUtil(
    private val context: Context,
    private val storage: FirebaseStorage
) {

    private lateinit var audioUri: String
    private var startAudioButton: WeakReference<ImageButton>? = null
    private var playButton: WeakReference<ImageButton>? = null
    private var pauseButton: WeakReference<ImageButton>? = null
    private var progressBar: WeakReference<SpinKitView>? = null
    private var seekBar: WeakReference<SeekBar>? = null

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isMediaPlayerPrepared: Boolean = false
    private var isMediaPlayerCompleted: Boolean = false

    private val handler = Handler()
    private val seekBarRunnable = object : Runnable {

        override fun run() {
            if (isMediaPlayerPrepared && mediaPlayer.isPlaying) {
                val curPosition = mediaPlayer.currentPosition
                seekBar?.get()?.progress = curPosition
            }

            /*if (isMediaPlayerCompleted) {
                seekBar?.get()?.progress = seekBar?.get()?.max ?: 0
            }*/

            // Keep running
            handler.postDelayed(this, SEEK_BAR_RUNNABLE_DELAY)
        }
    }

    init {
        handler.postDelayed(seekBarRunnable, SEEK_BAR_RUNNABLE_DELAY)
    }

    fun playAudio(
        audioUri: String,
        startAudioButton: ImageButton,
        playButton: ImageButton,
        pauseButton: ImageButton,
        progressBar: SpinKitView,
        seekBar: SeekBar
    ) {
        Timber.d("Playing audio uri: %s", audioUri)
        this.audioUri = audioUri
        this.startAudioButton = WeakReference(startAudioButton)
        this.playButton = WeakReference(playButton)
        this.pauseButton = WeakReference(pauseButton)
        this.progressBar = WeakReference(progressBar)
        this.seekBar = WeakReference(seekBar)
        setupViews()
        startPlayback()
    }

    private fun setupViews() {
        pauseButton?.get()?.setOnClickListener {
            if (isMediaPlayerPrepared) {
                playButton?.get()?.visibility = View.VISIBLE
                pauseButton?.get()?.visibility = View.GONE
                mediaPlayer.pause()
            }
        }

        playButton?.get()?.setOnClickListener {
            if (isMediaPlayerPrepared) {
                if (isMediaPlayerCompleted) {
                    isMediaPlayerCompleted = false
                    mediaPlayer.seekTo(0)
                }

                pauseButton?.get()?.visibility = View.VISIBLE
                playButton?.get()?.visibility = View.GONE
                mediaPlayer.start()
            }
        }

        seekBar?.get()?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isMediaPlayerPrepared)
                    mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    private fun startPlayback() {
        stopMediaPlayer()
        Timber.d("Starting playback")
        progressBar?.get()?.visibility = View.VISIBLE
        startAudioButton?.get()?.visibility = View.GONE
        playButton?.get()?.visibility = View.GONE
        pauseButton?.get()?.visibility = View.GONE

        if (audioUri.startsWith("gs") || audioUri.startsWith("https")) {
            loadFromFirebase()
        } else {
            val file = File(audioUri)
            loadFromFile(file)
        }
    }

    private fun loadFromFirebase() {
        val localFile = File.createTempFile("downloaded", "audio")
        Timber.d("Saving online audio to: %s", localFile.absolutePath)
        storage.getReferenceFromUrl(audioUri).getFile(localFile)
            .addOnSuccessListener {
                Timber.d("Audio downloaded: %s", it)
                loadFromFile(localFile)
            }.addOnFailureListener {
                Timber.e(it, "Audio download failed")
                onError(R.string.audio_error_download_failed)
            }
    }

    private fun onError(@StringRes errorMsgRes: Int) {
        Toast.makeText(context, errorMsgRes, Toast.LENGTH_LONG).show()
        progressBar?.get()?.visibility = View.GONE
        playButton?.get()?.visibility = View.GONE
        startAudioButton?.get()?.visibility = View.VISIBLE
    }

    private fun loadFromFile(audioFile: File) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.setOnPreparedListener {
                isMediaPlayerPrepared = true
                progressBar?.get()?.visibility = View.GONE
                pauseButton?.get()?.visibility = View.VISIBLE
                mediaPlayer.start()
                seekBar?.get()?.max = mediaPlayer.duration
            }
            mediaPlayer.setOnErrorListener { mediaPlayer, what, extra ->
                Timber.e("Error playing audio. What: %s, Extra: %s", what, extra)
                onError(R.string.audio_error_play_failed)
                true
            }
            mediaPlayer.setOnCompletionListener {
                Timber.d("MediaPlayer completed")
                isMediaPlayerCompleted = true
                playButton?.get()?.visibility = View.VISIBLE
                pauseButton?.get()?.visibility = View.GONE
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Timber.e(e, "Error playing audio")
            onError(R.string.audio_error_play_failed)
        }
    }

    fun stopAudio() {
        Timber.d("Stopping audio")
        stopMediaPlayer()

        playButton?.get()?.setOnClickListener(null)
        pauseButton?.get()?.setOnClickListener(null)
        seekBar?.get()?.setOnSeekBarChangeListener(null)

        progressBar?.get()?.visibility = View.GONE
        startAudioButton?.get()?.visibility = View.VISIBLE
        playButton?.get()?.visibility = View.GONE
        pauseButton?.get()?.visibility = View.GONE

        startAudioButton?.clear()
        playButton?.clear()
        pauseButton?.clear()
        progressBar?.clear()
        seekBar?.clear()
    }

    private fun stopMediaPlayer() {
        Timber.d("Stopping media player")
        if (isMediaPlayerPrepared) {
            mediaPlayer.stop()
            isMediaPlayerPrepared = false
            isMediaPlayerCompleted = false
        }
    }

    internal fun showAudioDialog(audioUri: String, sourceFragment: androidx.fragment.app.Fragment) {
        val ft = sourceFragment.fragmentManager?.beginTransaction()
        val newFragment = AudioPlayerFragment.newInstance(audioUri)
        if (ft != null) {
            newFragment.show(ft, "dialog")
        }
    }

}