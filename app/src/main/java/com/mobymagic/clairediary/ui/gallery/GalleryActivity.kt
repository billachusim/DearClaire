package com.mobymagic.clairediary.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ActivityGalleryBinding
import com.mobymagic.clairediary.util.GlideImageLoader
import com.veinhorn.scrollgalleryview.MediaInfo
import kotlinx.android.synthetic.main.layout_app_bar.*

class GalleryActivity : AppCompatActivity() {

    lateinit var binding: ActivityGalleryBinding
    lateinit var imageUrls: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.activity_gallery, null, false)
        setContentView(binding.root)

        imageUrls = intent.getStringArrayListExtra(ARG_IMAGES)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupGallery()
    }

    private fun setupGallery() {
        binding.scrollGalleryView
                .setThumbnailSize(100)
                .setZoom(true)
                .setFragmentManager(supportFragmentManager)
        for (url in imageUrls) {
            binding.scrollGalleryView.addMedia(MediaInfo.mediaLoader(GlideImageLoader(url)))
        }
    }

    companion object {
        val ARG_IMAGES = "ARG_IMAGES"
    }
}
