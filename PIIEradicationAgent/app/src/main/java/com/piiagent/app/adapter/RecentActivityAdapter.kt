package com.piiagent.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piiagent.app.databinding.ItemRecentActivityBinding
import com.piiagent.app.model.RecentActivityItem

class RecentActivityAdapter(
    private val items: List<RecentActivityItem>
) : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecentActivityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtTitle.text = item.title
        holder.binding.txtSubtitle.text = item.subtitle
        holder.binding.txtTimestamp.text = item.timestamp
        holder.binding.imgIcon.setImageResource(item.iconRes)
    }

    override fun getItemCount(): Int = items.size
}
