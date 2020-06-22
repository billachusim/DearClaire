package com.mobymagic.clairediary.ui.chatrooms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.fxn.pix.Pix
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mobymagic.clairediary.Constants.PREF_KEY_USER_AVATAR_URL
import com.mobymagic.clairediary.Constants.PREF_KEY_USER_ID
import com.mobymagic.clairediary.Constants.PREF_KEY_USER_NICKNAME
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.chatrooms.adapter.ChatAdapter
import com.mobymagic.clairediary.ui.chatrooms.adapter.ChatImageAdapter
import com.mobymagic.clairediary.ui.chatrooms.pojo.ChatRoom
import com.mobymagic.clairediary.ui.chatrooms.pojo.ChatRoomPojo
import com.mobymagic.clairediary.ui.chatrooms.util.Util.getDate
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.util.setBackgroundColorHex
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {

    private var isMessage: Boolean = false
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var message: String
    private lateinit var mIntent: ChatRoomPojo
    private lateinit var prefUtil: PrefUtil
    private val chatRoomList = LinkedList<ChatRoom>()
    private var mUpdate: Int = 0
    private lateinit var updateKey: String
    private lateinit var emojiPopup: EmojiPopup
    private val REQUEST_CODE_SELECT_PHOTO = 1
    private val MAX_PHOTOS = 3
    private var selectedPhotos = ArrayList<String>()
    private var storageRef = FirebaseStorage.getInstance().reference
    private lateinit var downloadUri: Uri

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mIntent = intent.getParcelableExtra("chatRoom")
        prefUtil = PrefUtil(this)
        comment_list.layoutManager = LinearLayoutManager(this)
        comment_list.itemAnimator = DefaultItemAnimator()

        image_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        image_list.itemAnimator = DefaultItemAnimator()

        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(session_detail_chat_input)

        this.title = mIntent.title
        toolbar.setBackgroundColorHex(mIntent.hex)
        window.statusBarColor = Color.parseColor(mIntent.hex)

        mDatabase = FirebaseDatabase.getInstance()

        session_detail_emoji_toggle_button.setOnClickListener {
            if (!emojiPopup.isShowing) {
                emojiPopup.toggle()
                session_detail_emoji_toggle_button.setImageResource(R.drawable.ic_keyboard_hide_white_24dp)
            } else {
                emojiPopup.dismiss()
                session_detail_emoji_toggle_button.setImageResource(R.drawable.ic_round_insert_emoticon_white_24)
            }
        }

        session_detail_chat_input.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                message = s.toString()

                if (message.isEmpty()) {
                    isMessage = false
                    session_detail_chat_button.visibility = View.GONE
                } else {
                    isMessage = true
                    session_detail_chat_button.visibility = View.VISIBLE
                    session_detail_chat_button.setImageResource(R.drawable.ic_round_send_24)
                }
            }
        })


        session_detail_add_photo_button.setOnClickListener {
            Pix.start(this, REQUEST_CODE_SELECT_PHOTO, MAX_PHOTOS)
        }

        session_detail_chat_button.setOnClickListener {
            when {
                mUpdate == 1 -> {
                    // update message
                    updateMessage(message, updateKey)
                    session_detail_chat_input.text!!.clear()
                }
                isMessage and selectedPhotos.isNotEmpty() -> {

                    for (img in selectedPhotos) {
                        sendImageMessage(img, message)
                    }
                    selectedPhotos.clear()
                    session_detail_chat_input.text!!.clear()
                    imageFrame.visibility = View.GONE
                    image_list.adapter = ChatImageAdapter(this@ChatActivity, selectedPhotos)
                }
                else -> {
                    sendTextMessage(message, "", 0)
                    session_detail_chat_input.text!!.clear()
                }
            }
        }

        try {
            readMessages()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*Receives broadcast*/
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("custom-message"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mClearImageList, IntentFilter("clear-msg"))
    }

    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            mUpdate = 1

            val message = intent.getStringExtra("message")
            val image = intent.getStringExtra("image")

            if (image.isNotEmpty()) {
                selectedPhotos.add(image)
                imageFrame.visibility = View.VISIBLE
                image_list.adapter = ChatImageAdapter(this@ChatActivity, selectedPhotos)
            }

            updateKey = intent.getStringExtra("key")
            session_detail_chat_input.setText(message)
        }
    }

    private var mClearImageList: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            selectedPhotos.clear()
            imageFrame.visibility = View.GONE
            image_list.adapter = ChatImageAdapter(this@ChatActivity, selectedPhotos)
        }
    }

    private fun sendTextMessage(message: String, image: String, type: Int) {

        indicator.visibility = View.VISIBLE

        mUpdate = 0

        val map = HashMap<Any, Any?>()
        map["text_message"] = message
        map["audio_message"] = ""
        map["image_message"] = image
        map["message_type"] = type
        map["time"] = getDate()
        map["sender_user_nick_name"] = prefUtil.getString(PREF_KEY_USER_NICKNAME, null)
        map["sender_uid"] = prefUtil.getString(PREF_KEY_USER_ID, null)
        map["userAvatarUrl"] = prefUtil.getString(PREF_KEY_USER_AVATAR_URL, null)


        val ref = mDatabase
                .getReference("messages")
                .child("group_message")
                .child(mIntent.title!!)
                .push()
        ref.setValue(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("successful")
                        try {
                            readMessages()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Timber.e(task.exception!!)
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                    indicator.visibility = View.INVISIBLE
                }
    }

    private fun sendImageMessage(s: String, message: String) {

        imageFrame.visibility = View.GONE
        indicator.visibility = View.VISIBLE

        val file = Uri.fromFile(File(s))
        val ref = storageRef.child("images/mountains.jpg")
        val uploadTask = ref.putFile(file)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUri = task.result!!
                sendTextMessage(message, "$downloadUri", 2)
            } else {
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
            }
            indicator.visibility = View.INVISIBLE
        }
    }

    private fun updateMessage(message: String, key: String) {

        indicator.visibility = View.VISIBLE

        mUpdate = 0

        val map = HashMap<String, Any?>()
        map["text_message"] = message
        map["audio_message"] = ""
        map["image_message"] = ""
        map["message_type"] = 0
        map["time"] = getDate()
        map["sender_user_nick_name"] = prefUtil.getString(PREF_KEY_USER_NICKNAME, null)
        map["sender_uid"] = prefUtil.getString(PREF_KEY_USER_ID, null)
        map["userAvatarUrl"] = prefUtil.getString(PREF_KEY_USER_AVATAR_URL, null)


        val ref = mDatabase
                .getReference("messages")
                .child("group_message")
                .child(mIntent.title!!)
                .child(key)
        ref.updateChildren(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("successful")
                        try {
                            readMessages()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Timber.e(task.exception!!)
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                    indicator.visibility = View.INVISIBLE
                }
    }

    private fun readMessages() {

        indicator.visibility = View.VISIBLE

        val ref = mDatabase
                .getReference("messages")
                .child("group_message")
                .child(mIntent.title!!)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Timber.d(p0.value.toString())

                chatRoomList.clear()

                for (snap in p0.children) {

                    val chat = p0.child(snap.key!!).getValue(ChatRoom::class.java)

                    val chatR = ChatRoom(chat!!.text_message!!,
                            chat.audio_message!!,
                            chat.image_message!!,
                            chat.time!!,
                            chat.sender_uid!!,
                            chat.sender_user_nick_name!!,
                            snap.key!!,
                            mIntent.title!!,
                            chat.message_type!!,
                            chat.userAvatarUrl!! as String)

                    chatRoomList.add(chatR)

                }

                indicator.visibility = View.INVISIBLE

                if (chatRoomList.isNotEmpty()) {
                    val myAdapter = ChatAdapter(this@ChatActivity, chatRoomList)
                    myAdapter.notifyDataSetChanged()

                    comment_list.scrollToPosition(chatRoomList.size - 1)

                    comment_list.adapter = myAdapter
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PHOTO && data != null) {
                selectedPhotos = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                imageFrame.visibility = View.VISIBLE
                image_list.adapter = ChatImageAdapter(this, selectedPhotos)
            }
        }
    }

}
