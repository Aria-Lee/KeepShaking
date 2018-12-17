package com.example.aria.keep_shaking

import android.animation.ObjectAnimator
import android.hardware.Sensor
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.net.Uri
import android.os.*
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.MediaController
import com.example.aria.keep_shaking.Utils.Companion.isFastDoubleClick
import kotlinx.android.synthetic.main.collection_dialog.view.*
import kotlinx.android.synthetic.main.record_dialog.view.*
import kotlinx.android.synthetic.main.result_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //取得體感(Sensor)服務使用權限
        beginTimerText.visibility = View.GONE
//        hideAndShowImage(beginTimerText, AlphaAnimation(1f, 0f), View.GONE)
        countText.visibility = View.GONE
//        hideAndShowImage(countText, AlphaAnimation(1f, 0f), View.GONE)
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        pref = SharePreference(this)
        coin = pref.getCoin()
        recordList.addAll(pref.getRecord())
        collectionData = pref.getCollection()
        coinText.text = "$coin"
        objectAnimator()
        initRunnable()
        startOrStop.setOnClickListener {
            intiHandler()
            startOrStop()
        }
        showRecord.setOnClickListener { if (!Utils.isFastDoubleClick()) showRecord() }
        showCollection.setOnClickListener { if (!Utils.isFastDoubleClick()) showCollection() }
    }

    private lateinit var mSensorManager: SensorManager//體感(Sensor)使用管理
    private var mSensor: Sensor? = null                 //體感(Sensor)類別
    private var mLastX: Float = 0.toFloat()                    //x軸體感(Sensor)偏移
    private var mLastY: Float = 0.toFloat()                    //y軸體感(Sensor)偏移
    private var mLastZ: Float = 0.toFloat()                    //z軸體感(Sensor)偏移
    private var mSpeed: Double = 0.toDouble()                 //甩動力道數度
    private var mLastUpdateTime: Long = 0           //觸發時間
    private var shakeNum = 0
    private var goalNum = 20
    private var perTime: Long = 3000
    private var beginNum = 3
    private var coin = 500
    private var costPerTime = 10
    private var resultIndex = 0
    private lateinit var handlerThread: HandlerThread
    private lateinit var looper: Looper
    private lateinit var handler: Handler
    private lateinit var beginTimer: Runnable
    private lateinit var shakeTimer: Runnable
    private lateinit var vibrator: Vibrator
    private lateinit var pref: SharePreference
    private var recordList = mutableListOf<RecordData>()
    lateinit var collectionData: CollectionData
    private val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN)
    private fun startOrStop() {
        if (coin <= 0) showAlertDialog("您沒有足夠的籌碼")
        else {
//            when (startOrStop.text) {
//                "START" -> {
            if (!Utils.isFastDoubleClick()) {
                coin -= costPerTime
                coinText.text = "$coin"
                pref.saveCoin(coin)
                recordList.add(RecordData(costPerTime, sdf.format(System.currentTimeMillis())))
                pref.saveRecord(recordList)
                beginTimerText.visibility = View.VISIBLE
                startOrStop.visibility = View.GONE
                showCollectionText.visibility = View.GONE
                showCollection.visibility = View.GONE
                showRecord.hide()
                handler.post(beginTimer)
            }
        }
//                "STOP" -> {
//                    finishShake()
//                }
//            }

//        }
    }

    fun initRunnable() {

        shakeTimer = Runnable {
            if (shakeNum > goalNum) {
                println("***************** $shakeNum")
                goalNum += goalNum
                handler.postDelayed(shakeTimer, perTime)
            } else {
                finishShake()
                goalNum = 20
            }

        }

        beginTimer = Runnable {

            if (beginNum > 0) {
                runOnUiThread {
                    beginTimerText.text = "$beginNum"
                    beginNum -= 1
                }
                handler.postDelayed(beginTimer, 1000)
            } else if (beginNum == 0) {
                runOnUiThread {
                    beginTimerText.visibility = View.GONE
                    countText.visibility = View.VISIBLE

                }
                initSensor()

                handler.removeCallbacks(beginTimer)
                println("***************** start")
                handler.postDelayed(shakeTimer, perTime)
            }
        }
    }

    private fun intiHandler() {
        handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        looper = handlerThread.looper
        handler = Handler(looper)
    }

    //甩動力道數度設定值 (數值越大需甩動越大力，數值越小輕輕甩動即會觸發)
    private val SPEED_SHRESHOLD = 3000

    //觸發間隔時間
    private val UPTATE_INTERVAL_TIME = 70

    private fun initSensor() {
        //取得手機Sensor狀態設定

        //註冊體感(Sensor)甩動觸發Listener
        mSensorManager!!.registerListener(SensorListener, mSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private val SensorListener = object : SensorEventListener {
        override fun onSensorChanged(mSensorEvent: SensorEvent) {
            //當前觸發時間
            val mCurrentUpdateTime = System.currentTimeMillis()

            //觸發間隔時間 = 當前觸發時間 - 上次觸發時間
            val mTimeInterval = mCurrentUpdateTime - mLastUpdateTime

            //若觸發間隔時間< 70 則return;
            if (mTimeInterval < UPTATE_INTERVAL_TIME) return

            mLastUpdateTime = mCurrentUpdateTime

            //取得xyz體感(Sensor)偏移
            val x = mSensorEvent.values[0]
            val y = mSensorEvent.values[1]
            val z = mSensorEvent.values[2]

            //甩動偏移速度 = xyz體感(Sensor)偏移 - 上次xyz體感(Sensor)偏移
            val mDeltaX = x - mLastX
            val mDeltaY = y - mLastY
            val mDeltaZ = z - mLastZ

            mLastX = x
            mLastY = y
            mLastZ = z

            //體感(Sensor)甩動力道速度公式
            mSpeed = Math.sqrt((mDeltaX * mDeltaX + mDeltaY * mDeltaY + mDeltaZ * mDeltaZ).toDouble()) / mTimeInterval *
                    10000

            //若體感(Sensor)甩動速度大於等於甩動設定值則進入 (達到甩動力道及速度)
            if (mSpeed >= SPEED_SHRESHOLD) {
                //達到搖一搖甩動後要做的事情
                shakeNum += 1
                if (shakeNum % 2 == 0) {
                    valueAnim.start()
                    countText.text = "${shakeNum / 2}"
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun finishShake() {
        vibrator.vibrate(500)
        mSensorManager!!.unregisterListener(SensorListener)
        handler.removeMessages(0)
        handlerThread.quitSafely()
        valueAnim.end()
        runOnUiThread {
            countText.text = "0"
            countText.visibility = View.GONE

            resultDialog()
//            startOrStop.text = "START"
            beginTimerText.visibility = View.GONE
            showCollection.visibility = View.VISIBLE
            showCollectionText.visibility = View.VISIBLE
            startOrStop.visibility = View.VISIBLE
            showRecord.show()
        }
    }

    private lateinit var valueAnim: ObjectAnimator
    private fun objectAnimator() {
        valueAnim = ObjectAnimator.ofFloat(shakeImage, "translationY", 0.0f, -720.0f, 0.0f)
        valueAnim.setDuration(500)
        valueAnim.interpolator = DecelerateInterpolator()
    }

    fun setVideo(view: View) {
        val vidControl = MediaController(this)
        val videoView = view.videoView
        vidControl.setAnchorView(videoView)
        vidControl.isShowing
        videoView.setMediaController(vidControl)

//        val index = collectionData.list.indexOf(collectionData.list.first{ it.videoId == id })
        collectionData.list[resultIndex].isUnclock = true
        pref.saveCollection(collectionData)
        val videoUri =
            Uri.parse("android.resource://" + getPackageName() + "/" + collectionData.list[resultIndex].videoId)


        videoView.setVideoURI(videoUri)

        videoView.setOnCompletionListener {
            videoView.start()
        }

        videoView.start()
    }

    fun resultDialog() {
        resultIndex = when (shakeNum / 2) {
            in 0..goalNum / 2 -> 0
            in goalNum / 2 + 1..goalNum / 2 * 2 -> 1
            in goalNum / 2 * 2 + 1..goalNum / 2 * 3 -> 2
            in goalNum / 2 * 3 + 1..goalNum / 2 * 4 -> 3
            in goalNum / 2 * 4 + 1..goalNum / 2 * 5 -> 4
            in goalNum / 2 * 5 + 1..goalNum / 2 * 6 -> 5
            in goalNum / 2 * 6 + 1..goalNum / 2 * 7 -> 6
            in goalNum / 2 * 7 + 1..goalNum / 2 * 8 -> 7
            else -> 8
        }
        val view = LayoutInflater.from(this).inflate(R.layout.result_dialog, null)
        setVideo(view.videoView)
        view.videoTitle.text = collectionData.list[resultIndex].name
        view.newRecord.text = "本次紀錄：${shakeNum / 2} 下"
        if (shakeNum / 2 > collectionData.max) {
            collectionData.max = shakeNum / 2
            pref.saveCollection(collectionData)
        }
        beginNum = 3
        shakeNum = 0
        resultIndex = 0

        AlertDialog.Builder(this)
            .setTitle("本次結果")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->

            }
            .show()
    }

    fun showRecord() {
        val view = LayoutInflater.from(this).inflate(R.layout.record_dialog, null)
        view.recordRecyclerView.layoutManager = LinearLayoutManager(this)
        view.recordRecyclerView.adapter = RecordAdapter(this, recordList)

        AlertDialog.Builder(this)
            .setTitle("消費紀錄")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .show()
    }

    fun showCollection() {
        val view = LayoutInflater.from(this).inflate(R.layout.collection_dialog, null)
        view.collectRecyclerView.layoutManager = GridLayoutManager(this, 3)
        val adpter = CollectionAdapter(this, collectionData)
        adpter.setOnClickItemListener(object : CollectionAdapter.OnClickItemListener {
            override fun onItemClick(index: Int) {
                if (!isFastDoubleClick()) {
                    resultIndex = index
                    showVideo()
                }
            }
        })
        view.collectRecyclerView.adapter = adpter
        view.maxText.text = "目前最高紀錄：${collectionData.max} 下"

        AlertDialog.Builder(this)
            .setTitle("收藏冊")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .show()
    }

    fun showVideo() {
        val view = LayoutInflater.from(this).inflate(R.layout.result_dialog, null)
        view.videoTitle.visibility = View.GONE
        view.newRecord.visibility = View.GONE
        setVideo(view.videoView)

        AlertDialog.Builder(this)
            .setTitle("${collectionData.list[resultIndex].name}")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .show()
    }

    fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("提醒")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which -> }
            .show()
    }

}
