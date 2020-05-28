package com.mobymagic.clairediary.util

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class ExoPlayerUtil(private val context: Context) : Player.DefaultEventListener() {


    var mainHandler = Handler()
    var bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
    var videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    var trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)


    init {

    }


    /**
     * return a new instance of the  player class with default settings
     */
    fun getPlayer(): SimpleExoPlayer {
        val player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player.addListener(this)
        player.playWhenReady = true
        return player
    }

    /**
     * provides a media source using the url of the audio
     */
    fun getMediaSource(audioUrl: String): MediaSource {
        // Measures bandwidth during playback. Can be null if not required.
        val bandwidthMeter = DefaultBandwidthMeter()
// Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Claire"), bandwidthMeter)
// This is the MediaSource representing the media to be played.
        return ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(audioUrl))
    }


}