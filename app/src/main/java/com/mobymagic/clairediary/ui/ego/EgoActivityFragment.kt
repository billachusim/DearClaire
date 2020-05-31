package com.mobymagic.clairediary.ui.ego


import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentEgoActivityBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.autoCleared
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject


class EgoActivityFragment : DataBoundNavFragment<FragmentEgoActivityBinding>() {
    private val appExecutors: AppExecutors by inject()
    private var egoFragmentActivityAdapter by autoCleared<EgoActivityAdapter>()
    private var egoFragmentUserActivityAdapter by autoCleared<EgoActivityAdapter>()
    private val egoViewModel: EgoViewModel by inject()
    private lateinit var userId: String

    override fun getLayoutRes() = R.layout.fragment_ego_activity

    override fun getPageTitle(): String {
        return "Dear Claire"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userId = requireArguments().getString(userIdKey).toString()
        egoViewModel.userId = userId
        createObservers()
        setupRecyclerView()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupRecyclerView() {
        egoFragmentActivityAdapter = EgoActivityAdapter(appExecutors, { it ->
            if (it != null) {
                egoViewModel.loadSessionById(it).observe(viewLifecycleOwner, Observer {
                    binding.activitySessionResource = it
                    when (it?.status) {
                        Status.SUCCESS -> {
                            if (it.data != null) {
                                getNavController()
                                        .navigateTo(SessionDetailFragment.newInstance(it.data, userId, SessionListType.ARCHIVED))
                            }

                        }

                        else -> {
                        }
                    }
                })
            }
        }, userId)
        egoFragmentUserActivityAdapter = EgoActivityAdapter(appExecutors, { it ->
            if (it != null) {
                egoViewModel.loadSessionById(it).observe(viewLifecycleOwner, Observer {
                    binding.activitySessionResource = it
                    when (it?.status) {
                        Status.SUCCESS -> {
                            if (it.data != null) {
                                getNavController()
                                        .navigateTo(SessionDetailFragment.newInstance(it.data, userId, SessionListType.ARCHIVED))
                            }

                        }

                        else -> {
                        }
                    }
                })
            }
        }, userId)
        binding.activitiesRecyclerView.adapter = egoFragmentActivityAdapter
        binding.myActivitiesRecyclerView.adapter = egoFragmentUserActivityAdapter
    }

    private fun createObservers() {
        egoViewModel.getUserActivity().observe(activity as LifecycleOwner, Observer {
            when (it?.status) {

                Status.SUCCESS -> {
                    egoFragmentActivityAdapter.submitList(it.data)
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })

        egoViewModel.getActivityByUser().observe(activity as LifecycleOwner, Observer {
            when (it?.status) {

                Status.SUCCESS -> {
                    egoFragmentUserActivityAdapter.submitList(it.data)
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment EgoActivityFragment.
         */
        const val userIdKey = "USER_ID"

        @JvmStatic
        fun newInstance(userId: String): EgoActivityFragment {
            return EgoActivityFragment().apply {
                val args = Bundle()
                args.putString(userIdKey, userId)
                this.arguments = args
            }
        }
    }
}
