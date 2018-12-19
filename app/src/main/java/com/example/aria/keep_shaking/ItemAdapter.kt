package com.example.aria.keep_shaking

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import kotlinx.android.synthetic.main.backgroundItem.view.*
import kotlinx.android.synthetic.main.collection.view.*
import kotlinx.android.synthetic.main.decorateItem.view.*
import kotlinx.android.synthetic.main.record.view.*

class ItemAdapter(val context: Context, var list: MutableList<ItemData>, val mode: Int) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    companion object {
        var DECOR = 0
        var BACKGROUND = 1
    }
    private var onClickItemListener : OnClickItemListener? = null
    interface OnClickItemListener{
        fun onItemClick(index : Int)
    }

    fun setOnClickItemListener(onClickItemListener: OnClickItemListener){
        this.onClickItemListener = onClickItemListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view : View? = null
        when(mode){
            DECOR -> view = LayoutInflater.from(context).inflate(R.layout.decorateItem, p0, false)
            BACKGROUND -> view = LayoutInflater.from(context).inflate(R.layout.backgroundItem, p0, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        when(mode){
            DECOR -> p0.bindDecor(list[p1])
            BACKGROUND -> p0.bindBackground(list[p1])
        }
//        if(data.list[p1].isUnclock == true) p0.imageView.setOnClickListener { if(!Utils.isFastDoubleClick()) onClickItemListener!!.onItemClick(p1) }
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
//        var decorStoreItemImageView: ImageView? = null
//        var radioButton: RadioButton? = null
//        var backgroundStoreItemImageView: ImageView? = null
//        var backgroundStoreItemCheckBox: CheckBox? = null

        fun bindDecor(data: ItemData) {
            var decorStoreItemImageView = itemView.decorateItemImageView
            var radioButton = itemView.radioButton
            decorStoreItemImageView.setImageResource(data.imageId)
        }

        fun bindBackground(data: ItemData){
            var backgroundStoreItemImageView = itemView.backgroundItemImageView
            var backgroundStoreItemCheckBox = itemView.backgroundItemCheckBox
            backgroundStoreItemImageView.setImageResource(data.imageId)
        }
    }
}