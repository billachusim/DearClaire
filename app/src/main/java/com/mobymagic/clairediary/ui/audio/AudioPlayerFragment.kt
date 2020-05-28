package com.mobymagic.clairediary.ui.audio


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.SimpleExoPlayer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAudioPlayerBinding
import com.mobymagic.clairediary.util.ExoPlayerUtil
import org.koin.android.ext.android.inject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val AUDIO_URL = "audio_url"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AudioPlayerFragment : androidx.fragment.app.DialogFragment() {
    private var audioUrl: String? = null
    private lateinit var player: SimpleExoPlayer
    private val exoPlayerUtil: ExoPlayerUtil by inject()
    lateinit var binding: FragmentAudioPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            audioUrl = it.getString(AUDIO_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.fragment_audio_player, null, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.audioFragmentClose.setOnClickListener {
            dismiss()
        }
        player = exoPlayerUtil.getPlayer()
        binding.sessionAudioView.player.player = player
        player.prepare(exoPlayerUtil.getMediaSource(audioUrl!!))


    }

    override fun onDestroy() {
        super.onDestroy()
        player.playWhenReady = false
        player.stop()
        player.release()
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
        player.stop()
        player.release()
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
        player.stop()
        player.release()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param audioUrl The url of the Audio to be played.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(audioUrl: String) =
                AudioPlayerFragment().apply {
                    arguments = Bundle().apply {
                        putString(AUDIO_URL, audioUrl)
                    }
                }
    }
}
