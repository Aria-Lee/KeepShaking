package com.example.aria.keep_shaking

import android.graphics.Bitmap

data class RecordData(val cost : Int, val time : String)
data class CollectionListElement(val imageId: Int, val videoId : Int, val name: String, var isUnclock: Boolean = false)
data class CollectionData(val list :MutableList<CollectionListElement>, var max : Int)