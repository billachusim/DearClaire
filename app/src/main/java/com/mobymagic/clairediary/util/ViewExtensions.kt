package com.mobymagic.clairediary.util

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import cn.nekocode.badge.BadgeDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.mobymagic.clairediary.GlideApp
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.vo.Session
import pl.digitalzombielab.dayview.DayView
import timber.log.Timber
import java.util.*


@BindingAdapter("visibleGone")
fun View.setVisibleOrGone(show: Boolean) {
    visibility = if (show) VISIBLE else GONE
}

@BindingAdapter("error")
fun setError(textInputLayout: TextInputLayout, errorMessage: String?) {
    textInputLayout.error = errorMessage
}

@BindingAdapter("spin")
fun View.setSpin(spinForever: Boolean) {
    val rotationAnim = AnimationUtils.loadAnimation(context, R.anim.spin)
    rotationAnim.fillAfter = true

    if (spinForever) {
        rotationAnim.repeatMode = Animation.RESTART
        rotationAnim.repeatCount = Animation.INFINITE
    }

    startAnimation(rotationAnim)
}

@BindingAdapter("spinCounterClockwise")
fun View.setSpinCounterClockwise(spinForever: Boolean) {
    val rotationAnim = AnimationUtils.loadAnimation(context, R.anim.spin_counter_clockwise)
    rotationAnim.fillAfter = true

    if (spinForever) {
        rotationAnim.repeatMode = Animation.RESTART
        rotationAnim.repeatCount = Animation.INFINITE
    }

    startAnimation(rotationAnim)
}

@BindingAdapter("cardBackgroundColorHex")
fun androidx.cardview.widget.CardView.setCardBackgroundColorHex(colorHex: String) {
    setCardBackgroundColor(Color.parseColor(colorHex))
}

@BindingAdapter("backgroundTintColor")
fun View.setBackgroundTintColor(colorHex: String) {
    background = ViewUtil.tintDrawable(background, Color.parseColor(colorHex))
}

@BindingAdapter("backgroundColorHex")
fun View.setBackgroundColorHex(colorHex: String?) {
    setBackgroundColor(Color.parseColor(colorHex))
}

@BindingAdapter("barColorHex")
fun DayView.setBarColorHex(colorHex: String) {
    barColor = Color.parseColor("#D3D3D3")
}

@BindingAdapter("date")
fun DayView.setDate(date: Date?) {
    if (date != null) {
        this.date = date
    }
}

@BindingAdapter("imageUrl")
fun ImageView.setImageUrl(imageUrl: String) {
    Timber.d("Image url: %s", imageUrl)
    if (imageUrl.startsWith("gs")) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        GlideApp.with(this).applyDefaultRequestOptions(RequestOptions().centerCrop())
            .load(storageRef).into(this)
    } else {
        GlideApp.with(this)
            .applyDefaultRequestOptions(
                RequestOptions().centerCrop().placeholder(R.drawable.default_thumbnail)
            )
            .load(imageUrl).into(this)
    }
}

@BindingAdapter("avatarUrl")
fun ImageView.setAvatarUrl(imageUrl: String) {
    Timber.d("Avatar url: %s", imageUrl)
    GlideApp.with(this)
        .applyDefaultRequestOptions(
            RequestOptions().placeholder(R.drawable.brown_boy_mask).centerCrop().fitCenter()
        )
        .load(imageUrl).into(this)
}

@BindingAdapter("timeAgo")
fun TextView.setTimeAgo(date: Date?) {
    if (date != null) {
        val now = System.currentTimeMillis()
        val timeAgo =
            DateUtils.getRelativeTimeSpanString(date.time, now, DateUtils.MINUTE_IN_MILLIS)
        text = timeAgo
    }
}

@BindingAdapter("font")
fun TextView.setFont(font: Typeface) {
    typeface = font
}

@BindingAdapter("html")
fun TextView.setHtml(html: String?) {
    text = if (html == null) null else HtmlUtil.fromHtml(html)
}

@BindingAdapter("startDrawable", "drawableTintColor")
fun TextView.setStartDrawable(topDrawable: Drawable, drawableTintColor: Int) {
    val drawable = ViewUtil.tintDrawable(topDrawable, drawableTintColor)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

// todo hacky hackie hacker
@BindingAdapter("mood")
fun TextView.setMood(session: Session) {
    var text = "<b>${session.userNickname}</b> "
    val mood = Mood.getMood(session.moodId)
    if (mood.id != Mood.NO_MODE_ID) {
        text += "<small>     ${mood.name}</small>"
    }
    setText(HtmlUtil.fromHtml(text))
}

// todo hacky hackie hacker
@BindingAdapter("mood1")
fun TextView.setMood1(session: Session) {
    var text = ""
    val mood = Mood.getMood(session.moodId)
    if (mood.id != Mood.NO_MODE_ID) {
        text += "<small>     ${mood.name}</small>"
    }
    setText(HtmlUtil.fromHtml(text))
}

@BindingAdapter("timeAgoWithDate")
fun TextView.setTimeAgoWithDate(date: Date?) {
    if (date != null) {
        val sessionDate = DateFormat.format("EEE. MMM d, yyyy. h:mm a", date)
        text = sessionDate
    }
}

@BindingAdapter("badgedText")
fun TextView.setBadgedText(text: String) {
    background = BadgeDrawable.Builder()
        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
        .badgeColor(R.color.theme_primary)
        .text1(text)
        .textSize(60.00f)
        .build()

}

@BindingAdapter("resourceId")
fun AppCompatImageView.setCompactSource(resourceId: Int) {
    setImageResource(resourceId)
}

@BindingAdapter("userAdminAvatarUrl")
fun ImageView.setUserAdminAvatarUrl(image: Any) {

    if (image is String) {
        Timber.d("Avatar url: %s", image)
        GlideApp.with(this)
            .applyDefaultRequestOptions(
                RequestOptions().placeholder(R.mipmap.ic_launcher).centerCrop()
            )
            .load(image).into(this)
    } else if (image is BitmapDrawable) {
        setImageDrawable(image)
    }

}

@BindingAdapter("adminBadge")
fun TextView.adminBadge(userType: String?) {

    if (userType != null) {
        if (userType == "Alter-Ego" || userType == "Super-Ego") {
            setBackgroundColor(resources.getColor(R.color.admin_badge_color))
        } else {
            setBackgroundColor(resources.getColor(R.color.user_badge_color))
        }
    }

}

