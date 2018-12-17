package com.example.aria.keep_shaking

class Utils {
    companion object {
        private var lastClickTime: Long = 0
        fun isFastDoubleClick(): Boolean {
            val time = System.currentTimeMillis()
            val timeD = time - lastClickTime
            if (0 < timeD && timeD < 1000) {
                return true
            }else{
                lastClickTime = time
                return false
            }
        }
    }
}