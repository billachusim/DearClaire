package com.mobymagic.clairediary.ui.alteregoorientation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAlterEgoOrientationBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.util.PrefUtil
import com.thejuki.kformmaster.helper.*
import com.thejuki.kformmaster.model.BaseFormElement
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject
import java.net.URLEncoder

class AlterEgoOrientationFragment : DataBoundNavFragment<FragmentAlterEgoOrientationBinding>() {

    private val prefUtil: PrefUtil by inject()
    private lateinit var formBuilder: FormBuildHelper

    override fun getLayoutRes() = R.layout.fragment_alter_ego_orientation

    override fun getPageTitle() = getString(R.string.alter_ego_orientation_page_title)

    override fun isAlterEgoPage() = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        formBuilder = form(context!!, binding.orientationRecyclerView) {

            header {
                title = getString(R.string.alter_ego_orientation_first_header)
            }
            text(FormTag.FULL_NAME.ordinal) {
                title = getString(R.string.alter_ego_orientation_full_name)
                required = true
            }
            text(FormTag.FULL_ADDRESS.ordinal) {
                title = getString(R.string.alter_ego_orientation_full_address)
                required = true
            }
            phone(FormTag.PHONE_NUMBER.ordinal) {
                title = getString(R.string.alter_ego_orientation_phone_number)
                required = true
            }
            number(FormTag.AGE.ordinal) {
                title = getString(R.string.alter_ego_orientation_age)
                required = true
            }
            text(FormTag.NAME_OF_SCHOOL_OR_OCCUPATION.ordinal) {
                title = getString(R.string.alter_ego_orientation_name_of_school_or_occupation)
                required = true
            }
            text(FormTag.NAME_OF_BEST_FRIEND_OR_RELATIVE.ordinal) {
                title = getString(R.string.alter_ego_orientation_best_friend_or_relative_name)
                required = true
            }
            phone(FormTag.PHONE_NUMBER_OF_BEST_FRIEND_OR_RELATIVE.ordinal) {
                title = getString(R.string.alter_ego_orientation_phone_of_best_friend_or_relative)
                required = true
            }
            number(FormTag.AMOUNT_DONATED.ordinal) {
                title = getString(R.string.alter_ego_orientation_amount_donated)
                required = true
            }
            email(FormTag.EMAIL_ADDRESS.ordinal) {
                title = getString(R.string.alter_ego_orientation_email_address)
                required = true
            }
            text(FormTag.FACEBOOK_NAME.ordinal) {
                title = getString(R.string.alter_ego_orientation_facebook_name)
            }
            text(FormTag.INSTAGRAM_USERNAME.ordinal) {
                title = getString(R.string.alter_ego_orientation_instagram_username)
            }
            textArea(FormTag.NAME_OF_BEST_FRIEND_OR_RELATIVE.ordinal) {
                title = getString(R.string.alter_ego_orientation_short_bio)
            }
            header {
                title = getString(R.string.alter_ego_orientation_second_header)
            }
            switch<String>(FormTag.INTERESTED_IN_RANDOM_POSTS.ordinal) {
                title = getString(R.string.alter_ego_orientation_random_posts)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.MAKE_THE_WORLD_A_BETTER_PLACE.ordinal) {
                title = getString(R.string.alter_ego_orientation_world_better_place)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.ARE_YOU_FOLLOWING_ON_INSTAGRAM.ordinal) {
                title = getString(R.string.alter_ego_orientation_following_on_instagram)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.LEARNED_ABOUT_CLAIRE_ON_INSTAGRAM.ordinal) {
                title = getString(R.string.alter_ego_orientation_learn_on_instagram)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.HAVE_YOU_RATED_ON_PLAYSTORE.ordinal) {
                title = getString(R.string.alter_ego_orientation_rated_on_playstore)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.DO_YOU_BELIEVE_IN_CLAIRE_PROJECT.ordinal) {
                title = getString(R.string.alter_ego_orientation_believe_claire_project)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            switch<String>(FormTag.ARE_YOU_READY_TO_BE_CLAIRE.ordinal) {
                title = getString(R.string.alter_ego_orientation_ready_to_be_claire)
                required = true
                value = "No"
                onValue = "Yes"
                offValue = "No"
            }
            header {
                title = getString(R.string.alter_ego_orientation_third_header)
            }
            button(FormTag.CONTINUE_TO_WHATSAPP.ordinal) {
                value = getString(R.string.alter_ego_orientation_action_continue_to_whatsapp)
                valueObservers.add { newValue, element ->
                    onContinueToWhatsAppClicked()
                }
            }
        }

        // formBuilder.cacheForm = true
    }

    private fun onContinueToWhatsAppClicked() {
        val whatsAppUrl = getWhatsAppUrl(getPayload())
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsAppUrl))
        prefUtil.setBool(Constants.PREF_KEY_COMPLETED_ALTER_EGO_ORIENTATION, true)
        startActivity(browserIntent)
        getNavController().remove(this)
        getNavController().removeFromBackstack(this)
    }

    private fun getWhatsAppUrl(payload: String): String {
        return Constants.WHATSAPP_URL + URLEncoder.encode(payload)
    }

    private fun getPayload(): String {
        val fullName = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.FULL_NAME.ordinal)
        val fullAddress = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.FULL_ADDRESS.ordinal)
        val phoneNumber = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.PHONE_NUMBER.ordinal)
        val age = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.AGE.ordinal)
        val nameOfSchoolOrOccupation =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.NAME_OF_SCHOOL_OR_OCCUPATION.ordinal)
        val nameOfFriendOrRelative =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.NAME_OF_BEST_FRIEND_OR_RELATIVE.ordinal)
        val phoneNumberFriendOrRelative =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.PHONE_NUMBER_OF_BEST_FRIEND_OR_RELATIVE.ordinal)
        val amountDonated = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.AMOUNT_DONATED.ordinal)
        val emailAddress = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.EMAIL_ADDRESS.ordinal)
        val facebookName = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.FACEBOOK_NAME.ordinal)
        val instagramUsername = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.INSTAGRAM_USERNAME.ordinal)
        val shortBio = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.SHORT_BIO.ordinal)
        val interestedRandomPosts =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.INTERESTED_IN_RANDOM_POSTS.ordinal)
        val makeWorldBetter =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.MAKE_THE_WORLD_A_BETTER_PLACE.ordinal)
        val followingOnInstagram =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.ARE_YOU_FOLLOWING_ON_INSTAGRAM.ordinal)
        val learnedOnInstagram =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.LEARNED_ABOUT_CLAIRE_ON_INSTAGRAM.ordinal)
        val ratedOnPlaystore =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.HAVE_YOU_RATED_ON_PLAYSTORE.ordinal)
        val believeInClaire =
                formBuilder.getFormElement<BaseFormElement<*>>(FormTag.DO_YOU_BELIEVE_IN_CLAIRE_PROJECT.ordinal)
        val readyToBeClaire = formBuilder.getFormElement<BaseFormElement<*>>(FormTag.ARE_YOU_READY_TO_BE_CLAIRE.ordinal)
        val userUid = arguments!!.getString(ARG_USER_ID)
        val email = prefUtil.getString(Constants.PREF_KEY_USER_EMAIL, null)

        val payload = """
            I'm ready for final Clairentation. This are my details:

            *UserId*: $userUid

            *Email*: $email

            *${fullName.title}*: ${fullName.value}
            
            *${fullAddress.title}*: ${fullAddress.value}

            *${phoneNumber.title}*: ${phoneNumber.value}

            *${age.title}*: ${age.value}

            *${nameOfSchoolOrOccupation.title}*: ${nameOfSchoolOrOccupation.value}

            *${nameOfFriendOrRelative.title}*: ${nameOfFriendOrRelative.value}

            *${phoneNumberFriendOrRelative.title}*: ${phoneNumberFriendOrRelative.value}

            *${amountDonated.title}*: ${amountDonated.value}

            *${emailAddress.title}*: ${emailAddress.value}

            *${facebookName.title}*: ${facebookName.value}

            *${instagramUsername.title}*: ${instagramUsername.value}

            *${shortBio.title}*: ${shortBio.value}

            *${interestedRandomPosts.title}*: ${interestedRandomPosts.value}

            *${makeWorldBetter.title}*: ${makeWorldBetter.value}

            *${followingOnInstagram.title}*: ${followingOnInstagram.value}

            *${learnedOnInstagram.title}*: ${learnedOnInstagram.value}

            *${ratedOnPlaystore.title}*: ${ratedOnPlaystore.value}

            *${believeInClaire.title}*: ${believeInClaire.value}

            *${readyToBeClaire.title}*: ${readyToBeClaire.value}
            """.trimIndent()
        return payload
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: String): AlterEgoOrientationFragment {
            return AlterEgoOrientationFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                arguments = args
            }
        }

    }

    private enum class FormTag {
        FULL_NAME,
        FULL_ADDRESS,
        PHONE_NUMBER,
        AGE,
        NAME_OF_SCHOOL_OR_OCCUPATION,
        NAME_OF_BEST_FRIEND_OR_RELATIVE,
        PHONE_NUMBER_OF_BEST_FRIEND_OR_RELATIVE,
        AMOUNT_DONATED,
        EMAIL_ADDRESS,
        FACEBOOK_NAME,
        INSTAGRAM_USERNAME,
        SHORT_BIO,
        INTERESTED_IN_RANDOM_POSTS,
        MAKE_THE_WORLD_A_BETTER_PLACE,
        ARE_YOU_FOLLOWING_ON_INSTAGRAM,
        LEARNED_ABOUT_CLAIRE_ON_INSTAGRAM,
        HAVE_YOU_RATED_ON_PLAYSTORE,
        DO_YOU_BELIEVE_IN_CLAIRE_PROJECT,
        ARE_YOU_READY_TO_BE_CLAIRE,
        CONTINUE_TO_WHATSAPP
    }
}