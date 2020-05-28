package com.mobymagic.clairediary.ui.alteregointro

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAlterEgoIntroSlideBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment

class AlterEgoIntroSlideFragment : DataBoundNavFragment<FragmentAlterEgoIntroSlideBinding>() {

    override fun getLayoutRes() = R.layout.fragment_alter_ego_intro_slide

    override fun getPageTitle(): Nothing? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alterEgoIntroSlideImage.setImageResource(arguments!!.getInt(ARG_DRAWABLE_RES))
        binding.alterEgoIntroSlideText.text = getString(arguments!!.getInt(ARG_TEXT_RES))
    }

    companion object {

        private const val ARG_DRAWABLE_RES = "ARG_DRAWABLE_RES"
        private const val ARG_TEXT_RES = "ARG_TEXT_RES"

        fun newInstance(@DrawableRes drawableRes: Int, @StringRes textRes: Int): AlterEgoIntroSlideFragment {
            return AlterEgoIntroSlideFragment().apply {
                val args = Bundle()
                args.putInt(ARG_DRAWABLE_RES, drawableRes)
                args.putInt(ARG_TEXT_RES, textRes)
                arguments = args
            }
        }
    }

}
