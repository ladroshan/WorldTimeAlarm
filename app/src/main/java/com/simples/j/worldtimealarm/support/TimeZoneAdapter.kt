package com.simples.j.worldtimealarm.support

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simples.j.worldtimealarm.R
import com.simples.j.worldtimealarm.utils.MediaCursor
import kotlinx.android.synthetic.main.time_zone_item.view.*
import java.util.*

/**
 * Created by j on 22/02/2018.
 *
 */
class TimeZoneAdapter(private var tzs: ArrayList<String>, private val context: Context): RecyclerView.Adapter<TimeZoneAdapter.ViewHolder>() {

    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.time_zone_item, parent, false))

    override fun getItemCount(): Int = tzs.size

    override fun getItemId(position: Int): Long = tzs[position].hashCode().toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val difference = TimeZone.getTimeZone(tzs[position].replace(" ", "_")).getOffset(System.currentTimeMillis()) - TimeZone.getDefault().getOffset(System.currentTimeMillis())

        val offset = if(TimeZone.getDefault() == TimeZone.getTimeZone(tzs[position].replace(" ", "_"))) context.resources.getString(R.string.current_time_zone)
        else MediaCursor.getOffsetOfDifference(context, difference, MediaCursor.TYPE_CURRENT)

        holder.country.text = tzs[position]
        holder.offset.text = offset
        holder.itemView.setOnClickListener { listener.onItemClick(it, position) }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var country: TextView = view.time_zone_country
        var offset: TextView = view.time_zone_offset
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}