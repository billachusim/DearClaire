package com.mobymagic.clairediary.ui.loadsession

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentLoadSessionBinding
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoadSessionFragment : DataBoundNavFragment<FragmentLoadSessionBinding>() {

    private val sessionRepository: SessionRepository by inject()
    private var sessionWithIdLiveData: LiveData<Resource<Session>>? = null

    override fun getLayoutRes() = R.layout.fragment_load_session

    override fun getPageTitle(): Nothing? = null

    override var requiresAuthentication: Boolean = false

    private var sessionListType: SessionListType = SessionListType.DIARY

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val sessionId = requireArguments().getString(ARG_SESSION_ID)
        val isAlterEgo = requireArguments().getString(IS_ALTER_EGO)

        // if the person is coming from alter ego then fake an alter ego tab
        if (isAlterEgo == "true") {
            sessionListType = SessionListType.ASSIGNED
        }
        if (sessionWithIdLiveData == null) {
            sessionWithIdLiveData = sessionId?.let { sessionRepository.getSessionWithId(it) }
            sessionWithIdLiveData?.observe(viewLifecycleOwner, Observer { sessionResource ->
                binding.resource = sessionResource
                if (sessionResource?.status == Status.SUCCESS) {
                    if (sessionResource.data != null) {
                        navigateToSessionDetail(sessionResource.data)
                    } else {
                        // TODO toast error if session with id not found
                    }
                }
            })
        }
    }

    private fun navigateToSessionDetail(session: Session) {
        Timber.d("Navigating to session detail: %s", session)
        sessionWithIdLiveData?.removeObservers(this)
        val userId = requireArguments().getString(ARG_USER_ID, null)
        val sessionDetailFragment = SessionDetailFragment.newInstance(session, userId, sessionListType)
        getNavController().removeFromBackstack(this)
        getNavController().navigate(sessionDetailFragment, true)
        getNavController().remove(this)
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_SESSION_ID = "ARG_SESSION_ID"
        private const val IS_ALTER_EGO = "IS_ALTER_EGO"

        fun newInstance(userId: String, sessionId: String, isAlterEgo: String?): LoadSessionFragment {
            return LoadSessionFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                args.putString(ARG_SESSION_ID, sessionId)
                args.putString(IS_ALTER_EGO, isAlterEgo)
                arguments = args
            }
        }
    }

}
