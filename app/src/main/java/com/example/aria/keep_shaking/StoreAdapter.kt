package com.example.aria.keep_shaking

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.decorateItem.view.*
import kotlinx.android.synthetic.main.store.view.*

class StoreAdapter(val context: Context, var list: MutableList<ItemData>) : RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    private var onClickItemListener: OnClickItemListener? = null

    interface OnClickItemListener {
        fun onItemClick(index: Int)
    }

    fun setOnClickItemListener(onClickItemListener: OnClickItemListener) {
        this.onClickItemListener = onClickItemListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.store, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(list[p1])
        p0.storeImageView.setOnClickListener { if (!Utils.isFastDoubleClick()) onClickItemListener!!.onItemClick(p1) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var storeImageView = itemView.storeImageView
        var storeImageName = itemView.storeImageName

        fun bind(data: ItemData) {
            storeImageView.setImageResource(data.imageId)
            storeImageName.text = data.name
            if (data.isChecked) itemView.hasBought.visibility = View.VISIBLE
        }

    }
}