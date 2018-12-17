package com.example.aria.keep_shaking

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.record.view.*

class RecordAdapter(val context: Context, var list: MutableList<RecordData>) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.record, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(list[p1])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cost = itemView.cost
        val costTime = itemView.costTime

        @SuppressLint("SetTextI18n")
        fun bind(data: RecordData) {
            cost.text = " - ${data.cost}"
            costTime.text = data.time
        }
    }
}