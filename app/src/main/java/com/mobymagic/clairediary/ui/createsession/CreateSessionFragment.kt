package com.mobymagic.clairediary.ui.createsession

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate
import cafe.adriel.androidaudiorecorder.model.AudioSource
import com.fxn.pix.Pix
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentCreateSessionBinding
import com.mobymagic.clairediary.databinding.LayoutDialogCreateSessionBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.loadsession.LoadSessionFragment
import com.mobymagic.clairediary.util.*
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.widgets.ItemOffsetDecoration
import com.vanniktech.emoji.EmojiPopup
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class CreateSessionFragment : DataBoundNavFragment<FragmentCreateSessionBinding>() {

    override var requiresAuthentication: Boolean = false

    private val appExecutors: AppExecutors by inject()
    private val createSessionViewModel: CreateSessionViewModel by inject()
    private val inputUtil: InputUtil by inject()
    private val audioUtil: AudioUtil by inject()
    private val fontFactory: FontFactory by inject()
    private val exoPlayerUtil: ExoPlayerUtil by inject()

    private var photoListAdapter by autoCleared<CreateSessionPhotoListAdapter>()
    private var fontListAdapter by autoCleared<CreateSessionFontListAdapter>()
    private var emojiPopup by autoCleared<EmojiPopup>()
    private val audioFile: File = File.createTempFile("audio", "record.wav")
    private var hasShownInactivityMessage = false
    private val handler = Handler()
    private val inactivityRunnable = Runnable {
        hasShownInactivityMessage = true
        if (isAdded) {
            Toast.makeText(context, R.string.create_session_inactivity_message, Toast.LENGTH_LONG)
                    .show()
        }
    }

    override fun getLayoutRes() = R.layout.fragment_create_session

    override fun getPageTitle() = getString(R.string.create_session_page_title)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSession()
    }

    private fun initSession() {
        val session = requireArguments().getParcelable<Session>(ARG_SESSION)
        createSessionViewModel.initSession(session)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initButtons()
        initEmojiPopup()
        initPhotoList()
        setupForm()
        observeSession()
        observeSubmitStatus()
        inputUtil.setupAutoResizeInput(
                binding.createSessionMessageInvisibleText,
                binding.createSessionMessageInput,
                getString(R.string.create_session_hint_type_message)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioUtil.stopAudio()
    }

    override fun onStart() {
        super.onStart()
        resetInactivityTimer()
    }

    override fun onPause() {
        super.onPause()
        cancelInactivityTimer()
    }

    private fun setupForm() {
        binding.createSessionMessageInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                resetInactivityTimer()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

    }

    private fun resetInactivityTimer() {
        if (!hasShownInactivityMessage) {
            cancelInactivityTimer()
            handler.postDelayed(inactivityRunnable, 10000)
        }
    }

    private fun cancelInactivityTimer() {
        handler.removeCallbacks(inactivityRunnable)
    }

    private fun observeSession() {
        createSessionViewModel.getDraftSession().observe(viewLifecycleOwner, Observer { session ->
            Timber.d("Draft session gotten: %s", session)
            binding.draftSession = session
            binding.createSessionMessageInput.typeface =
                    fontFactory.getFontWithName(session?.font).typeface

            // Update photo list
            photoListAdapter.submitList(session?.imageUrls)
            photoListAdapter.notifyDataSetChanged()

            binding.audioView.startAudioButton.setOnClickListener {
                session?.audioUrl?.let { audioUtil.showAudioDialog(it, this) }
            }
        })
    }

    private fun observeSubmitStatus() {
        createSessionViewModel.getSubmitStatus().observe(viewLifecycleOwner, Observer { sessionResource ->
            Timber.d("Submit session: %s", sessionResource)
            // Show loading view when loading
            binding.loadingResource = sessionResource

            if (sessionResource?.status == Status.ERROR) {
                // Show error SnackBar
                Snackbar.make(binding.root, sessionResource.message!!, Snackbar.LENGTH_LONG).show()
            } else if (sessionResource?.status == Status.SUCCESS) {
                // When session is successfully submitted, open session detail page
                navigateToSessionLoadPage(sessionResource.data!!)
            }
        })
    }

    private fun navigateToSessionLoadPage(session: Session) {
        getNavController().removeFromBackstack(this)
        val sessionLoadFragment = LoadSessionFragment.newInstance(session.userId.toString(), session.sessionId.toString(), "false")
        getNavController().navigate(sessionLoadFragment, true)
        getNavController().remove(this)
    }

    private fun initPhotoList() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.createSessionPhotoList.layoutManager = layoutManager
        binding.createSessionPhotoList.addItemDecoration(
                ItemOffsetDecoration(requireContext(), R.dimen.grid_spacing_regular)
        )

        photoListAdapter = CreateSessionPhotoListAdapter(
                appExecutors,
                {
                    // When image is clicked, open gallery
                    openImageGallery()
                },
                { imageUrl ->
                    // When image remove button is clicked, remove it from list
                    createSessionViewModel.removePhoto(imageUrl)
                }
        )

        binding.createSessionPhotoList.adapter = photoListAdapter
    }

    private fun openImageGallery() {
        /*val photoList = binding.draftSession!!.imageUrls
        val position = photoList.indexOf(imageUrl)
        ZGallery.with(context, photoList).setSelectedImgPosition(position).show()*/
    }

    private fun initButtons() {
        binding.createSessionAddPhotoButton.setOnClickListener {
            cancelInactivityTimer()
            Pix.start(activity, REQUEST_CODE_SELECT_PHOTO, MAX_PHOTOS)
        }

        binding.createSessionRecordAudioButton.setOnClickListener {
            cancelInactivityTimer()
            openAudioRecordPage()
        }

        binding.createSessionRemoveAudioButton.setOnClickListener {
            resetInactivityTimer()
            createSessionViewModel.removeAudioNote()
        }

        binding.createSessionSubmitButton.setOnClickListener {
            resetInactivityTimer()
            val title = binding.draftSession!!.title
            showTitleInputDialog(title.toString())
            createSessionViewModel.setMessage(binding.createSessionMessageInput.text.toString())
        }

        binding.createSessionChangeBackgroundButton.setOnClickListener {
            resetInactivityTimer()
            createSessionViewModel.setMessage(binding.createSessionMessageInput.text.toString())
            createSessionViewModel.toggleBackground()
        }

        binding.createSessionFontButton.setOnClickListener {
            createSessionViewModel.setMessage(binding.createSessionMessageInput.text.toString())
            resetInactivityTimer()
            showFontDialog()
        }
    }

    private fun showFontDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.create_session_title_select_font)

        val view = View.inflate(context, R.layout.layout_dialog_list, null)
        val dialogList = view.findViewById<RecyclerView>(R.id.dialog_list)
        builder.setView(view)
        val dialog = builder.show()

        val sessionFontListAdapter = CreateSessionFontListAdapter(appExecutors) { font ->
            createSessionViewModel.setFont(font)
            dialog.dismiss()
        }
        sessionFontListAdapter.submitList(fontFactory.getFonts())
        dialogList.adapter = sessionFontListAdapter
    }

    private fun initEmojiPopup() {
        emojiPopup = inputUtil.setupEmojiPopup(
                binding.root,
                binding.createSessionEmojiToggleButton,
                binding.createSessionMessageInput
        )

        // Toggle emoji popup when clicked
        binding.createSessionEmojiToggleButton.setOnClickListener {
            emojiPopup.toggle()
        }
    }

    private fun showTitleInputDialog(title: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.create_session_title_enter_title)

        val binding = DataBindingUtil.inflate<LayoutDialogCreateSessionBinding>(requireActivity().layoutInflater,
                R.layout.layout_dialog_create_session, null, false)
        binding.createSessionTitleInput.setText(title)
        builder.setView(binding.root)

        val arrayAdapter = activity?.let {
            ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    Mood.MOODS
            )
        }
        binding.createSessionMoodSpinner.adapter = arrayAdapter

        builder.setPositiveButton(getDialogText(binding.createSessionPrivacySwitch,
                binding.createSessionRepliesSwitch)) { dialog, _ ->
            // When submit is clicked save the input values and call submit
            // If user checked privacy, then session is not private, else session is private
            val isPrivate = !binding.createSessionPrivacySwitch.isChecked
            createSessionViewModel.setTitle(binding.createSessionTitleInput.text.toString())
            createSessionViewModel.setPrivacyMode(isPrivate)
            createSessionViewModel.setMode(Mood.MOODS[binding.createSessionMoodSpinner.selectedItemPosition])
            createSessionViewModel.setRepliesEnabled(binding.createSessionRepliesSwitch.isChecked)
            createSessionViewModel.submitSession()
            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }

        val dialog = builder.show()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = binding.createSessionTitleInput.text.length >= 5
        }

        binding.createSessionTitleInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = s.length >= 5
            }

        })

        val createSessionToggleChanged: CompoundButton.OnCheckedChangeListener =
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    dialog.getButton(Dialog.BUTTON_POSITIVE).text = getDialogText(binding.createSessionPrivacySwitch,
                            binding.createSessionRepliesSwitch)
                }
        binding.createSessionPrivacySwitch.setOnCheckedChangeListener(createSessionToggleChanged)
        binding.createSessionRepliesSwitch.setOnCheckedChangeListener(createSessionToggleChanged)
    }

    private fun getDialogText(createSessionPrivacySwitch: Switch,
                              createSessionRepliesSwitch: Switch): String {
        return if (createSessionPrivacySwitch.isChecked ||
                createSessionRepliesSwitch.isChecked) {
            getString(R.string.create_session_action_submit_and_save)
        } else {
            getString(R.string.create_session_action_submit)
        }
    }

    override fun onResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Timber.d("Result received(%d, %d, %s)", requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PHOTO && data != null) {
                val selectedPhotos = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                createSessionViewModel.addPhotos(selectedPhotos)
                return true
            } else if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
                createSessionViewModel.setAudioNote(audioFile.absolutePath)
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

    override fun onStop() {
        // Save session draft
        createSessionViewModel.setMessage(binding.createSessionMessageInput.text.toString())
        createSessionViewModel.saveDraft()

        emojiPopup.dismiss()
        super.onStop()
    }

    private fun openAudioRecordPage() {
        Dexter.withActivity(requireActivity())
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val color = ContextCompat.getColor(context!!, R.color.theme_primary)
                        AndroidAudioRecorder.with(activity)
                                .setFilePath(audioFile.absolutePath)
                                .setColor(color)
                                .setRequestCode(REQUEST_CODE_RECORD_AUDIO)
                                .setSource(AudioSource.MIC)
                                .setSampleRate(AudioSampleRate.HZ_16000)
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

    companion object {

        private const val REQUEST_CODE_SELECT_PHOTO = 1
        private const val REQUEST_CODE_RECORD_AUDIO = 2
        private const val MAX_PHOTOS = 7
        private const val ARG_SESSION = "ARG_SESSION"

        fun newInstance(session: Session? = null): CreateSessionFragment {
            val fragment = CreateSessionFragment()
            val args = Bundle()
            args.putParcelable(ARG_SESSION, session)
            fragment.arguments = args
            return fragment
        }

    }
}
