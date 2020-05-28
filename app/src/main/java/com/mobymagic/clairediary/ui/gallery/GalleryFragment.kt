package com.mobymagic.clairediary.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.GalleryFragmentBinding
import com.mobymagic.clairediary.util.GlideImageLoader
import com.veinhorn.scrollgalleryview.MediaInfo
import kotlinx.android.synthetic.main.layout_app_bar.*


class GalleryFragment : androidx.fragment.app.DialogFragment() {
    lateinit var binding: GalleryFragmentBinding
    lateinit var imageUrls: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.app.DialogFragment.STYLE_NORMAL, R.style.DialogFragmentAnimatedTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.gallery_fragment, null, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Answers().logCustom(CustomEvent("Gallery Opened"))
        imageUrls = arguments!!.getStringArrayList(ARG_IMAGES)
        setupGallery()
    }

    override fun getTheme(): Int {
        return R.style.DialogFragmentAnimatedTheme
    }

    private fun setupGallery() {
        binding.scrollGalleryView
                .setThumbnailSize(100)
                .setZoom(true)
                .setFragmentManager(activity?.supportFragmentManager)
        for (url in imageUrls) {
            binding.scrollGalleryView.addMedia(MediaInfo.mediaLoader(GlideImageLoader(url)))
        }
    }


    companion object {
        val ARG_IMAGES = "ARG_IMAGES"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(imagesUrl: MutableList<String>) =
                GalleryFragment().apply {
                    arguments = Bundle().apply {
                        putStringArrayList(ARG_IMAGES, imagesUrl as java.util.ArrayList<String>)
                    }
                }
    }
}