package com.simples.j.worldtimealarm.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.simples.j.worldtimealarm.ContentSelectorActivity
import com.simples.j.worldtimealarm.R
import com.simples.j.worldtimealarm.models.ContentSelectorViewModel
import kotlinx.android.synthetic.main.fragment_date_picker.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

class DatePickerFragment : Fragment(), TabLayout.OnTabSelectedListener, CalendarView.OnDateChangeListener, View.OnClickListener {

    private lateinit var fragmentContext: Context
    private lateinit var viewModel: ContentSelectorViewModel

    private var startDateTab: TabLayout.Tab? = null
    private var endDateTab: TabLayout.Tab? = null

    private var dateSet = ZonedDateTime.now()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        fragmentContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_picker, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            viewModel = ViewModelProvider(this)[ContentSelectorViewModel::class.java]
        }

        dateSet = dateSet.withZoneSameInstant(ZoneId.of(viewModel.timeZone))

        calendar.minDate = dateSet.toInstant().toEpochMilli()
        calendar.setOnDateChangeListener(this)

        val instant: Instant
        when(viewModel.currentTab) {
            0 -> {
                instant = Instant.ofEpochMilli(viewModel.startDate.value ?: System.currentTimeMillis())
                dateSet = ZonedDateTime.ofInstant(instant, ZoneId.of(viewModel.timeZone))
                calendar.date = dateSet.toInstant().toEpochMilli()
            }
            1 -> {
                instant = Instant.ofEpochMilli(viewModel.endDate.value ?: System.currentTimeMillis())
                dateSet = ZonedDateTime.ofInstant(instant, ZoneId.of(viewModel.timeZone))
                calendar.date = dateSet.toInstant().toEpochMilli()
            }
        }

        startDateTab = tab_layout.getTabAt(0)?.apply {
            view.findViewById<TextView>(R.id.title)?.text = getString(R.string.range_start)
        }
        endDateTab = tab_layout.getTabAt(1)?.apply {
            view.findViewById<TextView>(R.id.title)?.text = getString(R.string.range_end)
        }

        tab_layout.getTabAt(viewModel.currentTab)?.select()
        tab_layout.addOnTabSelectedListener(this)

        // Observe
        viewModel.startDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if(it > 0) setTabSummary(startDateTab, formatDate(it))
                else setTabSummary(startDateTab, getString(R.string.range_not_set))
            }
        })
        viewModel.endDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if(it > 0) setTabSummary(endDateTab, formatDate(it))
                else setTabSummary(endDateTab, getString(R.string.range_not_set))
            }
        })

        action.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.action -> {
                val s = viewModel.startDate.value
                val e = viewModel.endDate.value

                if(s != null && e != null) {
                    if(s > 0 && e > 0) {
                        if(s > e || s == e) {
                            Snackbar.make(fragment_layout, getString(R.string.end_date_earlier_than_start_date), Snackbar.LENGTH_SHORT)
                                    .setAnchorView(action)
                                    .show()
                            return
                        }
                    }
                }

                if(s != null && s < System.currentTimeMillis()) {
                    if(s > 0) {
                        Snackbar.make(fragment_layout, getString(R.string.start_date_and_time_is_wrong), Snackbar.LENGTH_SHORT)
                                .setAnchorView(action)
                                .show()
                        return
                    }
                }

                if(e != null && DateUtils.isToday(e)) {
                    if(e > 0) {
                        Snackbar.make(fragment_layout, getString(R.string.end_date_earlier_than_today), Snackbar.LENGTH_SHORT)
                                .setAnchorView(action)
                                .show()
                        return
                    }
                }

                val resultIntent = Intent().apply {
                    putExtra(ContentSelectorActivity.START_DATE_KEY, viewModel.startDate.value)
                    putExtra(ContentSelectorActivity.END_DATE_KEY, viewModel.endDate.value)
                }

                activity?.run {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        viewModel.currentTab = tab?.position ?: 0

        when(tab?.position) {
            0 -> {
                calendar.date = viewModel.startDate.value ?: System.currentTimeMillis()
            }
            1 -> {
                calendar.date = viewModel.endDate.value ?: System.currentTimeMillis()
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {
        when(viewModel.currentTab) {
            0 -> {
                viewModel.startDate.value = null
                setTabSummary(startDateTab, getString(R.string.range_not_set))
            }
            1 -> {
                viewModel.endDate.value = null
                setTabSummary(endDateTab, getString(R.string.range_not_set))
            }
        }
    }

    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
        dateSet = dateSet.withYear(year).withMonth(month+1).withDayOfMonth(dayOfMonth)

        when(viewModel.currentTab) {
            0 -> {
                viewModel.startDate.value = dateSet.toInstant().toEpochMilli()
            }
            1 -> {
                viewModel.endDate.value = dateSet.toInstant().toEpochMilli()
            }
        }
    }

    private fun setTabSummary(tab: TabLayout.Tab?, content: String?) {
        tab?.view?.findViewById<TextView>(R.id.summary)?.text = content
    }

    private fun formatDate(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val timeZoneDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of(viewModel.timeZone))

        return timeZoneDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    companion object {
        const val TAG = "ContentSelectorFragment"

        @JvmStatic
        fun newInstance() = DatePickerFragment()
    }
}