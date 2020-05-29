package com.mobymagic.clairediary.ui.sessiondetail

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.NOT_FOCUSABLE
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioSource
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.fxn.pix.Pix
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSessionDetailBinding
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.commentlist.CommentListFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.SharedViewModel
import com.mobymagic.clairediary.ui.createsession.CreateSessionFragment
import com.mobymagic.clairediary.ui.gallery.GalleryActivity
import com.mobymagic.clairediary.ui.geustego.GuestEgoFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.*
import com.mobymagic.clairediary.vo.Comment
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.widgets.ItemOffsetDecoration
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.util.*

class SessionDetailFragment : DataBoundNavFragment<FragmentSessionDetailBinding>() {

    override var requiresAuthentication: Boolean = false

    private val inputUtil: InputUtil by inject()
    private val appExecutors: AppExecutors by inject()
    private val audioUtil: AudioUtil by inject()
    private val exoPlayerUtil: ExoPlayerUtil by inject()
    private val fragmentUtil: FragmentUtils by inject()
    private val sessionDetailViewModel: SessionDetailViewModel by inject()
    private val fontFactory: FontFactory by inject()
    private val sessionRepository: SessionRepository by inject()
    private val authViewModel: AuthViewModel by inject()
    private lateinit var sharedViewModel: SharedViewModel

    private var commentPhotoListAdapter by autoCleared<CreateCommentPhotoListAdapter>()
    private var adapter by autoCleared<SessionDetailImageAdapter>()
    private var emojiPopup by autoCleared<EmojiPopup>()
    private lateinit var session: Session
    private lateinit var userId: String
    private lateinit var tabType: SessionListType
    private var shouldOpenCommentBox: Boolean = false
    private val audioFile: File = File.createTempFile("audio", "record.wav")
    private lateinit var sessionDetailPlayer: SimpleExoPlayer


    override fun getLayoutRes() = R.layout.fragment_session_detail

    override fun getPageTitle() = session.title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = arguments!!.getParcelable(ARG_SESSION)
        userId = arguments!!.getString(ARG_USER_ID)
        tabType = arguments!!.getSerializable(ARG_TAB_TYPE) as SessionListType
        shouldOpenCommentBox = arguments!!.getBoolean(SHOULD_OPEN_COMMENT_BOX)

        Timber.d("Boom: %s", session)
        setHasOptionsMenu(true)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioUtil.stopAudio()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.session_detail, menu)
        val isUserCreatorOfSession = session.userId == userId
        val showUnFeatureView = session.featured && isUserCreatorOfSession
        menu.findItem(R.id.menu_unfavorite_session).isVisible = showUnFeatureView
        menu.findItem(R.id.menu_edit_session).isVisible = isUserCreatorOfSession
        binding.showUnFeatureView = showUnFeatureView
        super.onCreateOptionsMenu(menu, inflater)
        sharedViewModel.getNumberOfComments().observe(this, Observer { numberOfComments ->
            if (numberOfComments != null && numberOfComments > MAXIMUM_NUMBER_OF_COMMENTS) {
                binding.sessionDetailCommentLayout.visibility = GONE
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_unfavorite_session -> {
                session.featured = false
                sessionRepository.updateSession(session)
                activity?.invalidateOptionsMenu()
                binding.session = session
                true
            }
            R.id.menu_edit_session -> {
                val editSessionFragment = CreateSessionFragment.newInstance(session)
                getNavController().navigateTo(editSessionFragment)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openAudioRecordPage() {
        Dexter.withActivity(activity!!)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val color = ContextCompat.getColor(context!!, R.color.theme_primary)
                        AndroidAudioRecorder.with(activity)
                                .setFilePath(audioFile.absolutePath)
                                .setColor(color)
                                .setRequestCode(REQUEST_CODE_RECORD_AUDIO)
                                .setSource(AudioSource.MIC)
                                .setAutoStart(true)
                                .setKeepDisplayOn(true)
                                .record()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                        Toast.makeText(
                                context!!,
                                R.string.common_audio_permission_needed,
                                Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(
                                context!!,
                                R.string.common_audio_permission_denied,
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }).check()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.userAvailable = !TextUtils.isEmpty(userId)
        binding.isAdmin = SessionListType.isAlterEgo(tabType)
        binding.session = session
        binding.isUserCreatorOfSession = session.userId == userId
        Timber.d("Session detail: %s", session)
        updateFollowText()

//        binding.sessionAudioView?.startAudioButton?.setOnClickListener {
//            session.audioUrl?.let { it1 -> audioUtil.showAudioDialog(it1, this) }
//        }
        sessionDetailPlayer = exoPlayerUtil.getPlayer()
        binding.sessionAudioView.player.player = sessionDetailPlayer
        binding.sessionAudioView.player.findViewById<ImageView>(R.id.exo_play)?.setOnClickListener {
            if (sessionDetailPlayer.currentPosition > 0) {

            } else {
                sessionDetailPlayer.prepare(binding.session?.audioUrl?.let { it1 ->
                    exoPlayerUtil.getMediaSource(it1)
                })
            }

            sessionDetailPlayer.seekTo(sessionDetailPlayer.currentPosition)
            sessionDetailPlayer.playWhenReady = true
            it.visibility = View.GONE
            binding.sessionAudioView.player.findViewById<ImageButton>(R.id.exo_pause)!!.visibility = View.VISIBLE
        }

        binding.sessionAudioView.player.findViewById<ImageButton>(R.id.exo_pause)?.setOnClickListener {
            sessionDetailPlayer.playWhenReady = false
            binding.sessionAudioView.player.findViewById<ImageButton>(R.id.exo_play)!!.visibility = View.VISIBLE
            it.visibility = View.GONE
        }





        binding.sessionDetailCommentRemoveAudioButton.setOnClickListener {
            sessionDetailViewModel.removeAudioNote()
        }

        val font = fontFactory.getFontWithName(session.font)
        binding.sessionDetailMessageText.setFont(font.typeface)
        if (font.fontName.contains("roboto", true)) {
            binding.sessionDetailMessageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        } else {
            binding.sessionDetailMessageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
        updateFollowText()
        binding.sessionDetailFollow.setOnClickListener {
            if (session.userId == userId) {
                Toast.makeText(context, "You cannot follow your own Session", Toast.LENGTH_LONG).show()
            } else {
                showFollowingDialog(binding)
            }

        }
        binding.sessionDetailUserImage.setOnClickListener {
            getNavController().navigateToWithAuth(GuestEgoFragment.newInstance(binding.session!!.userId,
                    "", binding.session!!.userNickname,
                    binding.session!!.userAvatarUrl, SessionListType.ARCHIVED))
        }



        fragmentUtil.addIfNotExist(
                childFragmentManager,
                R.id.session_detail_comment_list_container,
                CommentListFragment.newInstance(session, userId, tabType),
                "CommentListFragment"
        )

        setupMeTooControls()
        setupSessionPhotoList()
        setupCreateCommentPhotoList()
        setupEmojiPopup()
        if (!TextUtils.isEmpty(authViewModel.getUserId())) {
            setupCommentInput()
        } else {
            setupBlockedCommentInput()
        }
        observeDraftComment()
        observeSubmitStatus()

        if (shouldOpenCommentBox) {
            try {
                binding.sessionDetailCommentInput.requestFocus()
                val imm: InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            } catch (ex: Exception) {
                Timber.d("Error Opening Kwyboard: %s", ex.message)
            }
        }
    }

    private fun setupMeTooControls() {
        binding.sessionDetailMeTooButton.setOnClickListener {
            binding.session = sessionDetailViewModel.toggleMeToo(userId, session)
        }
    }

    fun editComment(comment: Comment) {
        sessionDetailViewModel.editComment(comment)
    }

    private fun observeDraftComment() {
        sessionDetailViewModel.getDraftComment().observe(this, Observer { comment ->
            Timber.d("Draft comment gotten: %s", comment)
            binding.draftComment = comment

            // Update photo list
            commentPhotoListAdapter.submitList(comment?.imageUrls)
            commentPhotoListAdapter.notifyDataSetChanged()




            binding.commentAudioView.startAudioButton.setOnClickListener {
                binding.draftComment?.audioUrl?.let { it1 -> audioUtil.showAudioDialog(it1, this) }
            }

        })
    }

    private fun setupCreateCommentPhotoList() {
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.sessionDetailCommentPhotoList.layoutManager = layoutManager
        binding.sessionDetailCommentPhotoList.addItemDecoration(
                ItemOffsetDecoration(context!!, R.dimen.grid_spacing_regular)
        )

        commentPhotoListAdapter = CreateCommentPhotoListAdapter(
                appExecutors,
                { imageUrl ->
                    // When image is clicked, open gallery
                    openImageGallery(imageUrl)
                },
                { imageUrl ->
                    // When image remove button is clicked, remove it from list
                    sessionDetailViewModel.removePhoto(imageUrl)
                }
        )

        binding.sessionDetailCommentPhotoList.adapter = commentPhotoListAdapter
    }

    private fun openImageGallery(imageUrl: String) {
        /*val photoList = binding.draftSession!!.imageUrls
        val position = photoList.indexOf(imageUrl)
        ZGallery.with(context, photoList).setSelectedImgPosition(position).show()*/
    }

    private fun observeSubmitStatus() {
        sessionDetailViewModel.getSubmitStatus().observe(this, Observer { commentResource ->
            Timber.d("Submit comment: %s", commentResource)
            // Show loading view when loading
            binding.loadingResource = commentResource

            if (commentResource?.status == Status.ERROR) {
                // Show error SnackBar
                Snackbar.make(binding.root, commentResource.message!!, Snackbar.LENGTH_LONG).show()
            } else if (commentResource?.status == Status.SUCCESS) {
                // Clear input when session is successfully submitted
                binding.sessionDetailCommentInput.text = null
            }
        })
    }

    private fun setupCommentInput() {
        if (SessionListType.isAlterEgo(tabType)) {
            binding.sessionDetailCommentInput.setHint(R.string.session_detail_hint_enter_advice)
        } else {
            binding.sessionDetailCommentInput.setHint(R.string.session_detail_hint_enter_reply)
        }

        binding.sessionDetailCommentInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (isCommentInputEmpty()) {
                    binding.sessionDetailCommentButton.setImageResource(R.drawable.ic_round_keyboard_voice_white_24)
                    binding.sessionDetailCommentButton.contentDescription =
                            getString(R.string.session_detail_content_desc_record_audio)
                } else {
                    binding.sessionDetailCommentButton.setImageResource(R.drawable.ic_round_send_24)
                    binding.sessionDetailCommentButton.contentDescription =
                            getString(R.string.session_detail_content_desc_submit_comment)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        })

        binding.sessionDetailAddPhotoButton.setOnClickListener {
            Pix.start(activity, REQUEST_CODE_SELECT_PHOTO, MAX_PHOTOS)
        }

        binding.sessionDetailCommentButton.setOnClickListener {
            if (isCommentInputEmpty()) {
                openAudioRecordPage()
            } else {
                val shouldAskToFollowPost: Boolean = !session.followers.contains(userId)
                        && session.userId != userId && !SessionListType.isAlterEgo(tabType)
                sessionDetailViewModel.setMessage(binding.sessionDetailCommentInput.text.toString())
                sessionDetailViewModel.submitComment(session, userId, SessionListType.isAlterEgo(tabType))

                //ask to follow post if it is his first comment
                if (shouldAskToFollowPost) {
                    showFollowingDialog(binding)
                }
            }
        }
    }

    /**
     * used to setup the comment input bix when the user is
     * not signed up so that comments would not be allowed
     */
    private fun setupBlockedCommentInput() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.sessionDetailCommentInput.focusable = NOT_FOCUSABLE
        }
        binding.sessionDetailCommentInput.hint = "Please Sign Up or Sign In to Comment"
        binding.sessionDetailCommentInput.setOnClickListener {
            authViewModel.getAuthRoute(this, activity as MainActivity)
        }
        binding.sessionDetailCommentInput.setOnFocusChangeListener { view: View, b: Boolean ->
            authViewModel.getAuthRoute(this, activity as MainActivity)
        }
    }

    private fun isCommentInputEmpty(): Boolean {
        return binding.sessionDetailCommentInput.text!!.isEmpty()
    }

    private fun setupSessionPhotoList() {
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.sessionDetailPhotoList.isNestedScrollingEnabled = false
        binding.sessionDetailPhotoList.layoutManager = layoutManager
        binding.sessionDetailPhotoList.addItemDecoration(
                ItemOffsetDecoration(context!!, R.dimen.grid_spacing_regular)
        )

        adapter = SessionDetailImageAdapter(appExecutors) { imageUrl ->
            val intent = Intent(activity, GalleryActivity::class.java).apply {
                putStringArrayListExtra(GalleryActivity.ARG_IMAGES, session.imageUrls as ArrayList<String>)
            }
            startActivity(intent)
        }

        binding.sessionDetailPhotoList.adapter = adapter
        adapter.submitList(session.imageUrls)
    }

    private fun setupEmojiPopup() {
        emojiPopup = inputUtil.setupEmojiPopup(
                binding.root,
                binding.sessionDetailEmojiToggleButton,
                binding.sessionDetailCommentInput
        )

        // Toggle emoji popup when clicked
        binding.sessionDetailEmojiToggleButton.setOnClickListener({
            emojiPopup.toggle()
        })
    }

    override fun onResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Timber.d("Result received(%d, %d, %s)", requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PHOTO && data != null) {
                val selectedPhotos = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                sessionDetailViewModel.addPhotos(selectedPhotos)
                return true
            } else if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
                sessionDetailViewModel.setAudioNote(audioFile.absolutePath)
                return true
            }
        }

        return false
    }

    override fun onBackPressed(): Boolean {
        if (emojiPopup.isShowing) {
            emojiPopup.dismiss()
            return true
        }

        return false
    }


    fun hideCommentBox() {
        binding.sessionDetailCommentLayout.visibility = GONE
    }

    companion object {

        private const val MAXIMUM_NUMBER_OF_COMMENTS = 300
        private const val REQUEST_CODE_SELECT_PHOTO = 1
        private const val REQUEST_CODE_RECORD_AUDIO = 2
        private const val MAX_PHOTOS = 7
        private const val ARG_SESSION = "ARG_SESSION"
        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_TAB_TYPE = "ARG_TAB_TYPE"
        private const val SHOULD_OPEN_COMMENT_BOX = "SHOULD_OPEN_COMMENT_BOX"

        fun newInstance(
                session: Session,
                userId: String,
                tabType: SessionListType,
                openCommentBox: Boolean = false
        ): SessionDetailFragment {
            Answers.getInstance().logCustom(CustomEvent("Session Detail Opened"))
            val sessionDetailFragment = SessionDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SESSION, session)
            args.putString(ARG_USER_ID, userId)
            args.putSerializable(ARG_TAB_TYPE, tabType)
            args.putBoolean(SHOULD_OPEN_COMMENT_BOX, openCommentBox)
            sessionDetailFragment.arguments = args
            return sessionDetailFragment
        }

    }

    private fun updateFollowText() {
        if (session.followers.contains(userId)) {
            binding.followText = "unfollow"
        } else {
            binding.followText = "follow"
        }
    }

    private fun showFollowingDialog(binding: FragmentSessionDetailBinding) {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(String.format(binding.root.context
                .getString(R.string.do_you_want_to_follow_this_session), binding.followText))
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            sessionDetailViewModel.toggleFollowers(userId, binding.session!!)
            Toast.makeText(binding.root.context,
                    if (binding.followText.equals("unfollow"))
                        "UnFollowed Diary Session" else {
                        "Following Diary Session"
                    }, Toast.LENGTH_LONG).show()
            updateFollowText(binding, binding.session!!)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        builder.show()
    }

    private fun updateFollowText(binding: FragmentSessionDetailBinding, session: Session) {

        if (session.followers.contains(userId)) {
            binding.followText = "unfollow"
        } else {
            binding.followText = "follow"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionDetailPlayer.playWhenReady = false
        sessionDetailPlayer.stop()
        sessionDetailPlayer.release()
    }

    override fun onStop() {
        emojiPopup.dismiss()
        super.onStop()
        sessionDetailPlayer.playWhenReady = false
        sessionDetailPlayer.stop()
        sessionDetailPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        sessionDetailPlayer.playWhenReady = false
        sessionDetailPlayer.stop()
        sessionDetailPlayer.release()
    }

}