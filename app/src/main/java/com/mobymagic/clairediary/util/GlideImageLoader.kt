package com.mobymagic.clairediary.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.mobymagic.clairediary.GlideApp
import com.mobymagic.clairediary.R
import com.veinhorn.scrollgalleryview.loader.MediaLoader

class GlideImageLoader(val imageUrl: String) : MediaLoader {
    override fun isImage(): Boolean {
        return true
    }

    override fun loadMedia(context: Context?, imageView: ImageView?, callback: MediaLoader.SuccessCallback?) {
        GlideApp.with(context!!)
                .applyDefaultRequestOptions(RequestOptions().placeholder(R.mipmap.ic_launcher))
                .load(imageUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target:
                    com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?,
                                                 target: com.bumptech.glide.request.target.Target<Drawable>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        //do something when picture already loaded
                        callback?.onSuccess()
                        return false
                    }
                })
                .into(imageView!!)
    }

    override fun loadThumbnail(context: Context?, thumbnailView: ImageView?, callback: MediaLoader.SuccessCallback?) {
        GlideApp.with(context!!)
                .applyDefaultRequestOptions(RequestOptions().placeholder(R.mipmap.ic_launcher))
                .load(imageUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target:
                    com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?,
                                                 target: com.bumptech.glide.request.target.Target<Drawable>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        //do something when picture already loaded
                        callback?.onSuccess()
                        return false
                    }
                })
                .into(thumbnailView!!)
    }
}