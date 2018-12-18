package com.example.aria.keep_shaking

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.achievement.view.*
import kotlinx.android.synthetic.main.achievement_title.view.*

class AchievementAdapter(val context: Context, var list: MutableList<AchievementData>) :
    RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {
    companion object {
        var SELFTitle = 0
        var SELFCONTENT = 1
        var COMMONTITLE = 2
        var COMMONCONTENT = 3

    }
    var mode = 0
    override fun getItemViewType(position: Int): Int {
        when(position){
            0 -> {
                mode = SELFTitle
            }
            in 1..4 ->{
                mode = SELFCONTENT
            }

            5 -> {
                mode = COMMONTITLE
            }

            else -> {
                mode = COMMONCONTENT
            }
        }
        return mode
    }

    override fun onCreateViewHolder(p0: ViewGroup, type: Int): ViewHolder {
        var view : View? = null
        when(type){
            SELFTitle -> {
                view = LayoutInflater.from(context).inflate(R.layout.achievement_title, p0, false)
            }

            SELFCONTENT -> {
                view = LayoutInflater.from(context).inflate(R.layout.achievement, p0, false)
            }

            COMMONTITLE -> {
                view = LayoutInflater.from(context).inflate(R.layout.achievement_title, p0, false)
            }

            COMMONCONTENT -> {
                view = LayoutInflater.from(context).inflate(R.layout.achievement, p0, false)
            }
        }
        return ViewHolder(view, type)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        when(mode){
            SELFTitle -> p0.bindTitle("遊戲成就")
            COMMONTITLE -> p0.bindTitle("帳戶成就")
            SELFCONTENT -> p0.bindContent(list[p1-1])
            COMMONCONTENT -> p0.bindContent(list[p1-2])
        }
    }

    class ViewHolder(itemView: View?, val mode: Int) : RecyclerView.ViewHolder(itemView!!) {
//        var achievementTitle : TextView? = null
//        var achievementName : TextView? = null
//        var achievementContent : TextView? = null

        @SuppressLint("SetTextI18n")
        fun bindContent(data: AchievementData) {
            var achievementName = itemView!!.achievementName
            var achievementContent = itemView!!.achievementContent
            achievementName.text = data.name
            achievementContent.text = data.content
        }

        fun bindTitle(title: String){
            var achievementTitle = itemView!!.achievementTitle
            achievementTitle.text = title
        }
    }
}