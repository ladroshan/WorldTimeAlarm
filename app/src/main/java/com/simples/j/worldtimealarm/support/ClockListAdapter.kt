package com.simples.j.worldtimealarm.support

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simples.j.worldtimealarm.R
import com.simples.j.worldtimealarm.etc.AlarmItem
import com.simples.j.worldtimealarm.etc.ClockItem
import kotlinx.android.synthetic.main.clock_list_item.view.*
import java.text.DateFormat
import java.util.*

/**
 * Created by j on 19/02/2018.
 *
 */
class ClockListAdapter(private var context: Context, private var list: ArrayList<ClockItem>, private var calendar: Calendar): RecyclerView.Adapter<ClockListAdapter.ViewHolder>() {

    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.clock_list_item, parent, false))
    }

    override fun getItemCount() = list.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeZone = TimeZone.getTimeZone(list[holder.adapterPosition].timezone.replace(" ", "_"))
        val expectedCalendar = Calendar.getInstance(timeZone)
        expectedCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        val differenceOriginal = calendar.timeZone.getOffset(System.currentTimeMillis()) - expectedCalendar.timeZone.getOffset(System.currentTimeMillis())
        expectedCalendar.add(Calendar.MILLISECOND, -differenceOriginal)

        holder.time_zone_name.text = expectedCalendar.timeZone.id
        val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
        timeFormat.timeZone = timeZone
        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG)
        dateFormat.timeZone = timeZone
        holder.time_zone_time.text = timeFormat.format(expectedCalendar.time)
        holder.time_zone_date.text = dateFormat.format(expectedCalendar.time)
    }

    fun removeItem(index: Int) {
        list.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, itemCount)
    }

    fun addItem(index: Int, item: ClockItem) {
        list.add(index, item)
        notifyItemInserted(index)
        notifyItemRangeChanged(index, itemCount)
    }

    fun setOnItemListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var time_zone_name: TextView = view.time_zone_name_clock
        var time_zone_time: TextView = view.time_zone_time
        var time_zone_date: TextView = view.time_zone_date
    }

    interface OnItemClickListener {
        fun onItemClicked(view: View, item: AlarmItem)
        fun onItemStatusChanged(b: Boolean, item: AlarmItem)
    }
}