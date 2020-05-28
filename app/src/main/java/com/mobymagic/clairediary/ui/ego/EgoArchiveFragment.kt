package com.mobymagic.clairediary.ui.ego


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentEgoArchiveBinding
import com.mobymagic.clairediary.ui.archivesessions.ArchiveSessionListFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 *
 */
class EgoArchiveFragment : DataBoundNavFragment<FragmentEgoArchiveBinding>(), CompactCalendarView.CompactCalendarViewListener {


    private val egoViewModel: EgoViewModel by inject()
    private lateinit var userId: String
    private var df: DateFormat = SimpleDateFormat("MMM yyyy")
    private val formatForDayComparison = SimpleDateFormat("yyyyMMdd")

    override fun getPageTitle(): String {
        return "Ego"
    }


    override fun getLayoutRes() = R.layout.fragment_ego_archive

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userId = requireArguments().getString(EgoActivityFragment.userIdKey)
        egoViewModel.userId = userId
        createObservers()
        createListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun createObservers() {
        egoViewModel.getUserSessionsByDate(userId,
                binding.compactcalendarView.firstDayOfCurrentMonth,
                Date()).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            eventObserverFunction(it)
        })

    }

    private fun createListeners() {
        binding.compactcalendarView.setListener(this)
        val currentDate = df.format(binding.compactcalendarView.firstDayOfCurrentMonth)
        binding.currentDate = currentDate
    }

    override fun onDayClick(dateClicked: Date?) {
        val evenForDay: List<Event> = binding.compactcalendarView.getEventsForMonth(dateClicked!!)
        val sessionsForDay: List<Session> = evenForDay.map {
            it.data as Session
        }.filter {
            formatForDayComparison.format(it.timeCreated) == formatForDayComparison.format(dateClicked)
        }
        getNavController()
                .navigate(ArchiveSessionListFragment.newInstance(userId, dateClicked), true)
    }

    override fun onMonthScroll(firstDayOfNewMonth: Date?) {
        binding.calenderDayTextView.text = binding.compactcalendarView.firstDayOfCurrentMonth.toString()
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = firstDayOfNewMonth
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val currentDate = df.format(binding.compactcalendarView.firstDayOfCurrentMonth)
        binding.currentDate = currentDate
        egoViewModel.retryLoadingUserSessionsByDate(userId, firstDayOfNewMonth!!, calendar.time)
                .observe(this, androidx.lifecycle.Observer {
                    eventObserverFunction(it)
                })
    }

    private fun eventObserverFunction(loadsessionResource: Resource<List<Session>>?) {
        when (loadsessionResource?.status) {
            Status.LOADING -> {
            }
            Status.ERROR -> {
            }
            Status.SUCCESS -> {
                if (loadsessionResource.data != null && loadsessionResource.data.size > 0) {
                    for (session in loadsessionResource.data) {
                        val ev1 = Event(Color.GREEN, session.timeCreated?.time!!,
                                session)
                        binding.compactcalendarView.addEvent(ev1)
                    }
                }
            }
        }
    }

    companion object {
        const val userIdKey = "USER_ID"

        @JvmStatic
        fun newInstance(userId: String) =
                EgoArchiveFragment().apply {
                    arguments = Bundle().apply {
                        putString(userIdKey, userId)
                    }
                }
    }
}
