package com.mobymagic.clairediary.ui.clairevartar


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentClaireVartarBinding
import com.mobymagic.clairediary.ui.createsession.ClaireVartarViewModel
import com.mobymagic.clairediary.util.autoCleared
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 *
 */
class ClaireVartarFragment : androidx.fragment.app.DialogFragment() {
    lateinit var binding: FragmentClaireVartarBinding
    private var adapter by autoCleared<ClaireVartarAdapter>()
    private val claireVartarViewModel: ClaireVartarViewModel by inject()
    private val appExecutors: AppExecutors by inject()
    private var selectedAvartar: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFragmentAnimatedTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.fragment_claire_vartar, null, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Answers().logCustom(CustomEvent("ClaireVartar Screen Opened"))
        initRecyclerView()
        initRecyclerViewAdapter()
    }


    private fun initRecyclerViewAdapter() {

        val rvAdapter = ClaireVartarAdapter(appExecutors
        ) { clairevartar ->
            selectedAvartar = clairevartar.imageUrl
            dismiss()
        }


        binding.claireVartarList.isNestedScrollingEnabled = false
        binding.claireVartarList.layoutManager = GridLayoutManager(context, 5)
        binding.claireVartarList.adapter = rvAdapter
        binding.claireVartarList.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL))
        binding.claireVartarList.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        adapter = rvAdapter
    }

    private fun initRecyclerView() {
        claireVartarViewModel.getClaireVartars().observe(this, Observer { result ->
            Timber.d("ClaireVartar resource: %s", result)
            adapter.submitList(result?.data)
            binding.executePendingBindings()
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        val intent = Intent()
        intent.putExtra(USER_AVARTAR_KEY, selectedAvartar)
        targetFragment?.onActivityResult(targetRequestCode,
                CLAIRE_VARTAR_RESULT_CODE, intent)
        super.onDismiss(dialog)
    }

    override fun getTheme(): Int {
        return R.style.DialogFragmentAnimatedTheme
    }

    companion object {

        const val CLAIRE_VARTAR_RESULT_CODE = 2000
        const val USER_AVARTAR_KEY = "USER_AVARTAR_KEY"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param "audioUrl" The url of the Audio to be played.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                ClaireVartarFragment().apply {

                }
    }

}
