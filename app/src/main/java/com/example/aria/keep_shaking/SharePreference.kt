package com.example.aria.keep_shaking

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SharePreference(val context: Context) {
    private val pref = context.getSharedPreferences("Keep Shaking", Context.MODE_PRIVATE)
    inline fun <reified T> Gson.fromJson(json: String?) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

    fun saveCoin(coin: Int) {
        pref.edit().putInt("coin", coin).apply()
    }

    fun saveRecord(record: MutableList<RecordData>){

        pref.edit().putString("recordString", Gson().toJson(record)).apply()
    }

    fun saveCollection(collection: CollectionData){
        pref.edit().putString("collectString", Gson().toJson(collection)).apply()
    }


    fun getCoin() : Int{
        return pref.getInt("coin", 500)
    }

    fun getRecord(): MutableList<RecordData>{
        val data = pref.getString("recordString",null)
        return  if(data!= null) return Gson().fromJson(data) else mutableListOf<RecordData>()
    }

    fun getCollection(): CollectionData{
        val data = pref.getString("collectString",null)
        return  if(data!= null) return Gson().fromJson(data) else CollectionData(initCollection(), 0)
    }

    fun initCollection(): MutableList<CollectionListElement>{
        val list = mutableListOf(
            CollectionListElement(getImageId("snail"), getVideoId("snail"),"Snail"),
            CollectionListElement(getImageId("turtle"), getVideoId("turtle"),"Turtle"),
            CollectionListElement(getImageId("hermit_crab"), getVideoId("hermit_crab"),"Hermit Crab"),
            CollectionListElement(getImageId("seal"), getVideoId("seal"),"Seal"),
            CollectionListElement(getImageId("peacock"), getVideoId("peacock"),"Peacock"),
            CollectionListElement(getImageId("cat_from_bag"), getVideoId("cat_from_bag"),"Cat"),
            CollectionListElement(getImageId("squirrel"), getVideoId("squirrel"),"Squirrel"),
            CollectionListElement(getImageId("little_bird"), getVideoId("little_bird"),"Little Bird"),
            CollectionListElement(getImageId("close_to_bird"), getVideoId("close_to_bird"),"Big Bird"))
        return list
    }

    fun getImageId(name: String): Int{
        return context.resources.getIdentifier(name, "drawable", context.packageName)

    }

    fun getVideoId(name: String): Int{
        println("**************** ${context.resources.getIdentifier(name, "raw", context.packageName)}")
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

}