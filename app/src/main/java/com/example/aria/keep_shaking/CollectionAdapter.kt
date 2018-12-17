package com.example.aria.keep_shaking

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.collection.view.*
import kotlinx.android.synthetic.main.record.view.*

class CollectionAdapter(val context: Context, var data: CollectionData) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    private var onClickItemListener : OnClickItemListener? = null
    interface OnClickItemListener{
        fun onItemClick(index : Int)
    }

    fun setOnClickItemListener(onClickItemListener: OnClickItemListener){
        this.onClickItemListener = onClickItemListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.collection, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(data.list[p1])
        p0.imageView.setOnClickListener { if(!Utils.isFastDoubleClick()) onClickItemListener!!.onItemClick(p1) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.collectionImageView
        val name = itemView.imageName

        @SuppressLint("SetTextI18n")
        fun bind(data: CollectionListElement) {
            if (data.isUnclock == true) {
                imageView.setImageResource(data.imageId)
                name.text = data.name
            } else {
                imageView.setImageResource(R.drawable.ic_help_black_24dp)
                name.text = "?????"
            }

        }
    }
}