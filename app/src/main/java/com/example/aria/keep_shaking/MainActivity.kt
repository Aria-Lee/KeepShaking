package com.example.aria.keep_shaking

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
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
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import com.example.aria.keep_shaking.Utils.Companion.isFastDoubleClick
import com.google.gson.Gson
import kotlinx.android.synthetic.main.achievement_dialog.view.*
import kotlinx.android.synthetic.main.collection_dialog.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.android.synthetic.main.itembox_dialog.view.*
import kotlinx.android.synthetic.main.record_dialog.view.*
import kotlinx.android.synthetic.main.result_dialog.view.*
import kotlinx.android.synthetic.main.store_dialog.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        decorList = listOf(showTree, showHouse, showSnail, showBird, showCow)
        okHttp = OkHttp(this)
        loadingRoot = window.decorView as ViewGroup
        loadingView = LayoutInflater.from(this).inflate(R.layout.loading_layout, loadingRoot, false)
        initStoreView()
        initItemBoxView()
        measureIntent()
        init()
        start.setOnClickListener {
            if (!Utils.isFastDoubleClick()) start()
        }


        showRecord.setOnClickListener { if (!Utils.isFastDoubleClick()) requestRecord() }
        showCollection.setOnClickListener { if (!Utils.isFastDoubleClick()) showCollection() }
        showAchievement.setOnClickListener { if (!Utils.isFastDoubleClick()) requestAchievement() }
        refreshBalance.setOnClickListener { if (!Utils.isFastDoubleClick()) requestRefresh() }
        showStore.setOnClickListener { if (!Utils.isFastDoubleClick()) showStore() }
        showItem.setOnClickListener { if (!Utils.isFastDoubleClick()) showItemBox() }
        decorList.forEach { it.setOnTouchListener(moveListener) }
    }

    private lateinit var mSensorManager: SensorManager//體感(Sensor)使用管理
    private var mSensor: Sensor? = null                 //體感(Sensor)類別
    private var mLastX: Float = 0.toFloat()                    //x軸體感(Sensor)偏移
    private var mLastY: Float = 0.toFloat()                    //y軸體感(Sensor)偏移
    private var mLastZ: Float = 0.toFloat()                    //z軸體感(Sensor)偏移
    private var mSpeed: Double = 0.toDouble()                 //甩動力道數度
    private var mLastUpdateTime: Long = 0           //觸發時間
    private var shakeNum = 0
    private var goalNum = 30
    private var gaolAddNum = 30
    private var perTime: Long = 3000
    private var beginNum = 3
    //    private var coin = 3
    private var costPerTime = 10
    private var resultIndex = 0
    private val gameId = 4
    private lateinit var requestData: JSONObject
    private lateinit var handlerThread: HandlerThread
    private lateinit var looper: Looper
    private lateinit var handler: Handler
    private lateinit var beginTimer: Runnable
    private lateinit var shakeTimer: Runnable
    private lateinit var vibrator: Vibrator
    private lateinit var pref: SharePreference
    private var buyItemId = 0
    private var reachLevel = 0
    private var recordList = mutableListOf<RecordData>()
    lateinit var collectionData: CollectionData
    lateinit var okHttp: OkHttp
    lateinit var responseData: JSONObject
    lateinit var responseArrayData: JSONArray
    lateinit var userInfo: UserInfo
    private val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN)
    private lateinit var loadingView: View
    private lateinit var loadingRoot: ViewGroup
    private fun start() {
        loadingRoot.addView(loadingView)
        requestData = JSONObject()
        requestData.put("api_token", userInfo.token)
        requestData.put("game_id", gameId)
        val json = requestData.toString()
        okHttp.request(json, "/api/play", ::start, OkHttp.RequestType.POST)
    }

    fun start(jsonObject: JSONObject) {
        runOnUiThread {

            if (jsonObject.get("result") == "success") {
                responseData = jsonObject.get("data") as JSONObject
//                coin = responseData.getInt("balance")
//                coinText.text = "$coin"
                coinText.text = "${responseData.getInt("balance")}"
//                pref.saveCoin(coin)
//                recordList.add(RecordData(costPerTime, sdf.format(System.currentTimeMillis())))
//                pref.saveRecord(recordList)
                intiHandler()
                beginTimerText.visibility = View.VISIBLE
                start.visibility = View.INVISIBLE
                showCollectionText.visibility = View.INVISIBLE
                showCollection.visibility = View.INVISIBLE
                showAchievement.visibility = View.INVISIBLE
                showStore.visibility = View.INVISIBLE
                showItem.visibility = View.INVISIBLE
                achievementText.visibility = View.INVISIBLE
                achievementText3.visibility = View.INVISIBLE
                achievementText2.visibility = View.INVISIBLE
                showRecord.hide()
                handler.post(beginTimer)
                loadingRoot.removeView(loadingView)
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun measureIntent() {
        if (intent.getStringExtra("From") == "AutoLogin") {
            loadingRoot.addView(loadingView)
            requestData = JSONObject()
            requestData.put("api_token", intent.getStringExtra("Token"))
            val json = requestData.toString()
            okHttp.request(json, "/api/autologin", ::autoLogin, OkHttp.RequestType.POST)
        } else {
            userInfo = Gson().fromJson(intent.getStringExtra("UserInfo"), UserInfo::class.java)
            initUserInfo()
        }
    }

    fun initUserInfo() {
        supportActionBar!!.title = "${userInfo.name} 的跳跳傑特"
//        coin = userInfo.balance
//        coin = 500
        coinText.text = "${userInfo.balance}"
        initRequestItem()
    }

    fun autoLogin(jsonObject: JSONObject) {
        runOnUiThread {

            if (jsonObject.get("result") == "success") {
                responseData = jsonObject.get("data") as JSONObject
                val name = responseData.getString("name")
                val balance = responseData.getInt("balance")
                val token = responseData.getString("api_token")
                pref.saveToken(token)
                userInfo = UserInfo(name, balance, token)
                loadingRoot.removeView(loadingView)
                initUserInfo()
            } else {
                loadingRoot.removeView(loadingView)
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
                pref.removeToken()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }
    }

    fun init() {

        //取得體感(Sensor)服務使用權限
        beginTimerText.visibility = View.GONE
        countText.visibility = View.GONE
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        pref = SharePreference(this)
//        coin = pref.getCoin()
//        recordList.addAll(pref.getRecord())
        collectionData = pref.getCollection()
//        coinText.text = "$coin"
        objectAnimator()
        initRunnable()
    }

    fun initRunnable() {

        shakeTimer = Runnable {
            if (shakeNum > goalNum) {
                println("************** shakeNum $shakeNum")
                println("************** goalNum $goalNum")
                goalNum += gaolAddNum
                gaolAddNum += 6
                if (resultIndex < 8) resultIndex += 1
                println("************** resultIndex $resultIndex")
                handler.postDelayed(shakeTimer, perTime)
            } else {
                finishShake()
                goalNum = 30

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
            //            resultIndex()
            loadingRoot.addView(loadingView)

            requestData = JSONObject()
            requestData.put("api_token", userInfo.token)
            requestData.put("achieve_id", collectionData.list[resultIndex].id)
//        requestData.put("acheivement_id", RecordData[resultIndex].id)
            println("************** requestData $resultIndex")

            val json = requestData.toString()
            okHttp.request(json, "/api/achievement", ::finish, OkHttp.RequestType.POST)
        }
    }

    fun finish(jsonObject: JSONObject) {
        runOnUiThread {

            if (jsonObject.get("result") == "success") {
                collectionData.list[resultIndex].isUnclock = true
                countText.text = "0"
                countText.visibility = View.GONE
                resultDialog()
//            start.text = "START"
                beginTimerText.visibility = View.GONE
                showCollection.visibility = View.VISIBLE
                showCollectionText.visibility = View.VISIBLE
                start.visibility = View.VISIBLE
                showAchievement.visibility = View.VISIBLE
                achievementText.visibility = View.VISIBLE
                showStore.visibility = View.VISIBLE
                showItem.visibility = View.VISIBLE
                achievementText3.visibility = View.VISIBLE
                achievementText2.visibility = View.VISIBLE
                showRecord.show()
            } else Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            loadingRoot.removeView(loadingView)
        }
    }

    private lateinit var valueAnim: ObjectAnimator
    private fun objectAnimator() {
        valueAnim = ObjectAnimator.ofFloat(jett, "translationY", 0.0f, -720.0f, 0.0f)
        valueAnim.setDuration(500)
        valueAnim.interpolator = DecelerateInterpolator()
    }

    fun setVideo(view: View) {
        val vidControl = MediaController(this)
        val videoView = view.videoView
        vidControl.setAnchorView(videoView)
        vidControl.isShowing
        videoView.setMediaController(vidControl)

//        collectionData.list[resultIndex].isUnclock = true
        val videoUri =
            Uri.parse("android.resource://" + getPackageName() + "/" + collectionData.list[resultIndex].videoId)


        videoView.setVideoURI(videoUri)

        videoView.setOnCompletionListener {
            videoView.start()
        }

        videoView.start()
    }

    fun resultDialog() {

        val view = LayoutInflater.from(this).inflate(R.layout.result_dialog, null)
        setVideo(view.videoView)
        view.videoTitle.text = collectionData.list[resultIndex].name
        view.newRecord.text = "本次紀錄：${shakeNum / 2} 下"
        if (shakeNum / 2 > collectionData.max) {
            collectionData.max = shakeNum / 2
            println("************* resultDialog ${collectionData.max}")
//            pref.saveMax(collectionData.max)
        }
        beginNum = 3
        shakeNum = 0
        resultIndex = 0
        gaolAddNum = 30


        AlertDialog.Builder(this)
            .setTitle("本次結果")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .show()
    }

    fun requestRecord() {
        loadingRoot.addView(loadingView)
        okHttp.request(userInfo.token, "/api/detail", ::showRecord, OkHttp.RequestType.GET)
    }

    fun showRecord(jsonObject: JSONObject) {
        runOnUiThread {
            recordList.clear()

            if (jsonObject.get("result") == "success") {
                responseArrayData = jsonObject.get("data") as JSONArray
                for (i in 0 until responseArrayData.length()) {
                    responseData = responseArrayData[i] as JSONObject
                    recordList.add(
                        RecordData(
                            responseData.getInt("game_id"),
                            responseData.getString("description"),
                            responseData.getInt("amount"),
                            responseData.getString("updated_at")
                        )
                    )
                }
                val view = LayoutInflater.from(this).inflate(R.layout.record_dialog, null)
                view.recordRecyclerView.layoutManager = LinearLayoutManager(this)
                view.recordRecyclerView.adapter = RecordAdapter(this, recordList)

                val titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
                titleView.dialogTitle.text = "消費紀錄"
                titleView.setBackgroundColor(Color.rgb(255, 255, 255))
                AlertDialog.Builder(this)
                    .setCustomTitle(titleView)
                    .setView(view)
                    .setPositiveButton("OK") { dialog, which ->
                    }
                    .show()
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
            loadingRoot.removeView(loadingView)
        }
    }

    fun requestCollection() {
        loadingRoot.addView(loadingView)
        okHttp.request(userInfo.token, "/api/achievement", ::initCollection, OkHttp.RequestType.GET)
    }

    fun initCollection(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                responseArrayData = jsonObject.get("data") as JSONArray
                val idList = mutableListOf<Int>()
                for (i in 0 until responseArrayData.length()) {
                    responseData = responseArrayData[i] as JSONObject
                    idList.add(responseData.getInt("achieve_id"))
                }
                for (i in 0 until collectionData.list.size) {
                    if (collectionData.list[i].id in idList) collectionData.list[i].isUnclock = true
                }
            } else Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            loadingRoot.removeView(loadingView)
        }

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

        val titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
        titleView.dialogTitle.text = "收藏冊"
        titleView.setBackgroundColor(Color.rgb(255, 255, 255))

        AlertDialog.Builder(this)
            .setTitle("收藏冊")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .show()
    }

    val achievementList = mutableListOf(
        AchievementData(1, "試水溫", "遊玩總次數達 1 次", false),
        AchievementData(2, "新手", "遊玩總次數達 10 次", false),
        AchievementData(3, "駕輕就熟", "遊玩總次數達 50 次", false),
        AchievementData(4, "老手", "遊玩總次數達 100 次", false),
        AchievementData(5, "遊戲成癮者", "遊玩總次數達 200 次", false),
        AchievementData(6, "低消", "儲值總金額達 50", false),
        AchievementData(7, "半張小朋友", "儲值總金額達 500", false),
        AchievementData(8, "好野人", "儲值總金額達 1000", false),
        AchievementData(9, "土豪", "儲值總金額達 2000", false)
    )

    fun requestAchievement() {
        loadingRoot.addView(loadingView)
        okHttp.request(userInfo.token, "/api/achievement", ::showAchievement, OkHttp.RequestType.GET)
    }

    fun showAchievement(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                responseArrayData = jsonObject.get("data") as JSONArray
                val idList = mutableListOf<Int>()
                for (i in 0 until responseArrayData.length()) {
                    responseData = responseArrayData[i] as JSONObject
                    idList.add(responseData.getInt("achieve_id"))
                }
                for (i in 0 until achievementList.size) {
                    if (achievementList[i].id in idList) achievementList[i].isUnclock = true
                }
                val view = LayoutInflater.from(this).inflate(R.layout.achievement_dialog, null)
                view.achievementRecyclerView.layoutManager = LinearLayoutManager(this)
                view.achievementRecyclerView.adapter = AchievementAdapter(this, achievementList)

                val titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
                titleView.dialogTitle.text = "成就"
                titleView.setBackgroundColor(Color.rgb(255, 255, 255))

                AlertDialog.Builder(this)
                    .setCustomTitle(titleView)
                    .setView(view)
                    .setPositiveButton("OK") { dialog, which ->
                    }
                    .show()
            } else Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            loadingRoot.removeView(loadingView)
        }
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
            .setOnDismissListener { resultIndex = 0 }
            .show()
    }

    fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("提醒")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which -> }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            loadingRoot.addView(loadingView)
            requestData = JSONObject()
            requestData.put("api_token", userInfo.token)
            val json = requestData.toString()
            okHttp.request(json, "/api/logout", ::logout, OkHttp.RequestType.DELETE)
        }

        if (item.itemId == R.id.how) {
            AlertDialog.Builder(this)
                .setTitle("遊戲說明")
                .setMessage("點擊下方的 GO 按鈕 \n倒數三秒後開始快速搖動你的手機直到手機震動為止！\n \n * 此遊戲有九個成就階段，每個階段成功後將自動進入下個階段 \n\n * 階段失敗時將震動提醒遊戲結束 \n\n * 遊戲結算將依達成的最高階段解鎖獎勵小短片 \n\n * 可於收集冊查看所有已獲得小短片")
                .setPositiveButton("OK") { dialog, which -> }
                .show()
        }
        return true
    }

    fun logout(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                pref.removeToken()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            loadingRoot.removeView(loadingView)
        }
    }

//    fun requestShowStore() {
//        loadingRoot.addView(loadingView)
//        okHttp.request(userInfo.token, "/api/shop/$gameId", ::showStore, OkHttp.RequestType.GET)
//    }

    fun showStore() {
//        runOnUiThread {
//            if (jsonObject.get("result") == "success") {
//                responseArrayData = jsonObject.get("data") as JSONArray
//                val idList = mutableListOf<Int>()
//                var index: Int
//                for (i in 0 until responseArrayData.length()) {
//                    responseData = responseArrayData[i] as JSONObject
//                    idList.add(responseData.getInt("item_id"))
//                }
//                for (i in 0 until idList.size) {
//                    index = itemList.indexOf(itemList.first { it.id == idList[i] })
//                    itemList[index].isBought = true
//                    itemList[index].boughtView.visibility = View.VISIBLE
//                    itemList[buyIndex].cardView.isClickable = false
//                }
//            } else {
//                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
//            }

        val view = LayoutInflater.from(this).inflate(R.layout.base_dialog, null) as ViewGroup
        view.addView(storeView)

        val titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
        titleView.dialogTitle.text = "商店"

        AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setCustomTitle(titleView)
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .setOnDismissListener { view.removeAllViews() }
            .show()
        loadingDialogRoot = view
//            loadingRoot.removeView(loadingView)
//        }
    }

    val itemList = mutableListOf<ItemData>()
    lateinit var storeView: View
    fun initStoreView() {
        storeView = LayoutInflater.from(this).inflate(R.layout.store_dialog, null)
        itemList.addAll(
            mutableListOf(
                ItemData(
                    0,
                    storeView.tieCard,
                    storeView.tieHasBought,
                    storeView.tiePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    1,
                    storeView.hatCard,
                    storeView.hatHasBought,
                    storeView.hatPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    2,
                    storeView.balloonCard,
                    storeView.balloonHasBought,
                    storeView.balloonPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    3,
                    storeView.sunglassesCard,
                    storeView.sunglassesHasBought,
                    storeView.sunglassesPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    4,
                    storeView.sheepCard,
                    storeView.sheepHasBought,
                    storeView.sheepPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    5,
                    storeView.treeCard,
                    storeView.treeHasBought,
                    storeView.treePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    6,
                    storeView.houseCard,
                    storeView.houseHasBought,
                    storeView.housePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    7,
                    storeView.snailCard,
                    storeView.snailHasBought,
                    storeView.snailPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    8,
                    storeView.birdCard,
                    storeView.birdHasBought,
                    storeView.birdPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    9,
                    storeView.cowCard,
                    storeView.cowHasBought,
                    storeView.cowPrice.text.toString().toInt(),
                    false,
                    false
                )
            )
        )

        itemList.forEach { item ->
            item.cardView.setOnClickListener {
                if (!Utils.isFastDoubleClick()) {
                    println("*************** click")
                    buyIndex = itemList.indexOf(item)
                    loadingDialogRoot!!.addView(loadingView)
                    requestData = JSONObject()
                    requestData.put("game_id", gameId)
                    requestData.put("item_id", item.id)
                    requestData.put("cost", item.consum)
                    requestData.put("api_token", userInfo.token)
                    val json = requestData.toString()
                    okHttp.request(json, "/api/shop", ::buyItem, OkHttp.RequestType.POST)
                }
            }
        }

    }

    var loadingDialogRoot: ViewGroup? = null

    var buyIndex = 0

    fun buyItem(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                responseData = jsonObject.get("data") as JSONObject
                coinText.text = responseData.getString("balance")
                itemList[buyIndex].boughtView.visibility = View.VISIBLE
                itemBoxList[buyIndex].boughtView.visibility = View.GONE
                itemList[buyIndex].isBought = true
                itemBoxList[buyIndex].isBought = true
                itemList[buyIndex].cardView.isClickable = false
//                if (itemBoxList[buyIndex].id in 0 until 4) {
                itemBoxList[buyIndex].cardView.isClickable = true
//                }
//                when (itemList[buyIndex].id) {
//                    5 -> showTree.visibility = View.VISIBLE
//                    6 -> showHouse.visibility = View.VISIBLE
//                    7 -> showSnail.visibility = View.VISIBLE
//                    8 -> showBird.visibility = View.VISIBLE
//                    9 -> showCow.visibility = View.VISIBLE
//                }
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
            loadingDialogRoot!!.removeView(loadingView)
        }
    }

    fun requestRefresh() {
        loadingRoot.addView(loadingView)
        okHttp.request(userInfo.token, "/api/balance", ::refresh, OkHttp.RequestType.GET)
    }

    fun refresh(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                responseData = jsonObject.get("data") as JSONObject
                coinText.text = "${responseData.getInt("balance")}"
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
            loadingRoot.removeView(loadingView)
        }
    }

    lateinit var itemBoxView: View
    var itemBoxList = mutableListOf<ItemData>()
    fun initItemBoxView() {
        itemBoxView = LayoutInflater.from(this).inflate(R.layout.itembox_dialog, null)
        itemBoxList.addAll(
            mutableListOf(
                ItemData(
                    0,
                    itemBoxView.boxtieCard,
                    itemBoxView.boxtieHasBought,
                    storeView.tiePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    1,
                    itemBoxView.boxhatCard,
                    itemBoxView.boxhatHasBought,
                    storeView.hatPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    2,
                    itemBoxView.boxballoonCard,
                    itemBoxView.boxballonHasBought,
                    storeView.balloonPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    3,
                    itemBoxView.boxsunglassesCard,
                    itemBoxView.boxsunglassesHasBought,
                    storeView.sunglassesPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    4,
                    itemBoxView.boxsheepCard,
                    itemBoxView.boxsheepHasBought,
                    storeView.sheepPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    5,
                    itemBoxView.boxtreeCard,
                    itemBoxView.boxtreeHasBought,
                    storeView.treePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    6,
                    itemBoxView.boxhouseCard,
                    itemBoxView.boxhouseHasBought,
                    storeView.housePrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    7,
                    itemBoxView.boxsnailCard,
                    itemBoxView.boxsnailHasBought,
                    storeView.snailPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    8,
                    itemBoxView.boxbirdCard,
                    itemBoxView.boxbirdHasBought,
                    storeView.birdPrice.text.toString().toInt(),
                    false,
                    false
                ),
                ItemData(
                    9,
                    itemBoxView.boxcowCard,
                    itemBoxView.boxcowHasBought,
                    storeView.cowPrice.text.toString().toInt(),
                    false,
                    false
                )
            )
        )

        itemBoxView.treecheckBox.isChecked = itemBoxList[5].isUsed
        itemBoxView.housecheckBox.isChecked = itemBoxList[6].isUsed
        itemBoxView.snailcheckBox.isChecked = itemBoxList[7].isUsed
        itemBoxView.birdcheckBox.isChecked = itemBoxList[8].isUsed
        itemBoxView.cowcheckBox.isChecked = itemBoxList[9].isUsed

        itemBoxList.take(5).forEach { item ->
            item.cardView.setOnClickListener {
                println("********************8click")

                itemBoxList.take(5).forEach {
                    if (it.id == item.id) item.isUsed = !item.isUsed
                    else it.isUsed = false
                }
                itemBoxView.tieRadioButton.isChecked = itemBoxList[0].isUsed
                itemBoxView.hatRadioButton.isChecked = itemBoxList[1].isUsed
                itemBoxView.balloonRadioButton.isChecked = itemBoxList[2].isUsed
                itemBoxView.sunglassesRadioButton.isChecked = itemBoxList[3].isUsed
                itemBoxView.sheepRadioButton.isChecked = itemBoxList[4].isUsed

            }
        }
        itemBoxList.takeLast(5).forEach { item ->
            item.cardView.setOnClickListener {
                item.isUsed = !item.isUsed
                when (item.id) {
                    5 -> itemBoxView.treecheckBox.isChecked = item.isUsed
                    6 -> itemBoxView.housecheckBox.isChecked = item.isUsed
                    7 -> itemBoxView.snailcheckBox.isChecked = item.isUsed
                    8 -> itemBoxView.birdcheckBox.isChecked = item.isUsed
                    9 -> itemBoxView.cowcheckBox.isChecked = item.isUsed
                }
            }
        }

    }


//
//    fun requestShowItemBox() {
//        loadingRoot.addView(loadingView)
//        okHttp.request(userInfo.token, "/api/shop/$gameId", ::showItemBox, OkHttp.RequestType.GET)
//    }

    fun showItemBox() {
//        runOnUiThread {
//            if (jsonObject.get("result") == "success") {
//                responseArrayData = jsonObject.get("data") as JSONArray
//                val idList = mutableListOf<Int>()
//                var index: Int
//                for (i in 0 until responseArrayData.length()) {
//                    responseData = responseArrayData[i] as JSONObject
//                    idList.add(responseData.getInt("item_id"))
//                }
//                for (i in 0 until idList.size) {
//                    index = itemBoxList.indexOf(itemBoxList.first { it.id == idList[i] })
//                    itemBoxList[index].isBought = true
//                    itemBoxList[index].boughtView.visibility = View.GONE
////                    itemBoxList[index].cardView.isClickable = true
//                    if (itemBoxList[index].id in 0 until 4) {
//                        itemBoxList[index].cardView.isClickable = true
//
//                    }
//                    println("*****************itemBoxList ${itemBoxList[index].id}")
//                }
//
//            } else {
//                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
//            }

        val view = LayoutInflater.from(this).inflate(R.layout.base_dialog, null) as ViewGroup
        view.addView(itemBoxView)

        val titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
        titleView.dialogTitle.text = "物品盒"

        AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setCustomTitle(titleView)
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
            }
            .setOnDismissListener {
                view.removeAllViews()

                if (itemBoxList.filter { it.isUsed == true }.size == 0) jett.setImageResource(R.drawable.sheep)
                else {
                    itemBoxList.take(5).forEach {
                        if (it.isUsed) {
                            when (it.id) {
                                0 -> jett.setImageResource(R.drawable.sheep_tie)
                                1 -> jett.setImageResource(R.drawable.sheep_hat)
                                2 -> jett.setImageResource(R.drawable.sheep_ballon)
                                3 -> jett.setImageResource(R.drawable.sheep_sunglasses)
                                4 -> jett.setImageResource(R.drawable.sheep_sheep)
                            }
                        }
                    }
                }


                itemBoxList.takeLast(5).forEach {
                    if (it.isUsed) {
                        when (it.id) {
                            5 -> showTree.visibility = View.VISIBLE
                            6 -> showHouse.visibility = View.VISIBLE
                            7 -> showSnail.visibility = View.VISIBLE
                            8 -> showBird.visibility = View.VISIBLE
                            9 -> showCow.visibility = View.VISIBLE
                        }
                    } else {
                        when (it.id) {
                            5 -> showTree.visibility = View.GONE
                            6 -> showHouse.visibility = View.GONE
                            7 -> showSnail.visibility = View.GONE
                            8 -> showBird.visibility = View.GONE
                            9 -> showCow.visibility = View.GONE
                        }
                    }
                }
            }
            .show()
//            loadingRoot.removeView(loadingView)
//        }

    }

//    fun setMainView() {
//        itemBoxList.forEach {
//            when (it.id) {
////                in 0 until 4 -> {
////                    if (it.isUsed) {
////                        when (it.id) {
////                            0 -> jett.setImageResource(R.drawable.sheep_tie)
////                            1 -> jett.setImageResource(R.drawable.sheep_hat)
////                            2 -> jett.setImageResource(R.drawable.sheep_ballon)
////                            3 -> jett.setImageResource(R.drawable.sheep_sunglasses)
////                            4 -> jett.setImageResource(R.drawable.sheep_sheep)
////                        }
////                    }
////                }
//
//                in 5 until 9 -> {
//                    if (it.isBought) {
//                        when (it.id) {
//                            5 -> showTree.visibility = View.VISIBLE
//                            6 -> showHouse.visibility = View.VISIBLE
//                            7 -> showSnail.visibility = View.VISIBLE
//                            8 -> showBird.visibility = View.VISIBLE
//                            9 -> showCow.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            }
//
//        }
//
//    }

    fun initRequestItem() {
//        println("************ ${loadingView.parent}")
        loadingRoot.addView(loadingView)
        okHttp.request(userInfo.token, "/api/shop/$gameId", ::initItem, OkHttp.RequestType.GET)
    }

    fun initItem(jsonObject: JSONObject) {
        runOnUiThread {
            if (jsonObject.get("result") == "success") {
                responseArrayData = jsonObject.get("data") as JSONArray
                val idList = mutableListOf<Int>()
                var index: Int
                for (i in 0 until responseArrayData.length()) {
                    responseData = responseArrayData[i] as JSONObject
                    idList.add(responseData.getInt("item_id"))
                }
                for (i in 0 until idList.size) {
                    index = itemBoxList.indexOf(itemBoxList.first { it.id == idList[i] })
                    itemBoxList[index].isBought = true
                    itemList[index].isBought = true
                    itemBoxList[index].boughtView.visibility = View.GONE
                    itemList[index].boughtView.visibility = View.VISIBLE
                    itemList[index].cardView.isClickable = false

//                    when (idList[i]) {
//                        5 -> showTree.visibility = View.VISIBLE
//                        6 -> showHouse.visibility = View.VISIBLE
//                        7 -> showSnail.visibility = View.VISIBLE
//                        8 -> showBird.visibility = View.VISIBLE
//                        9 -> showCow.visibility = View.VISIBLE
//                    }
//                    if (itemBoxList[index].id in 0 until 4) {
                    itemBoxList[index].cardView.isClickable = true

//                    }

                }
//                setMainView()
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
            loadingRoot.removeView(loadingView)
            requestCollection()
        }

    }

    var xToSub = 0f
    var yToSub = 0f
    var decorList = listOf<ImageView>()
    var x = 0f
    var y = 0f

    val moveListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    xToSub = event.rawX - v!!.x
                    yToSub = event.rawY - v.y
                }

                MotionEvent.ACTION_MOVE -> {

                    x = event.rawX - xToSub
                    if (x + v!!.width > (v.parent as View).width) x = ((v.parent as View).width - v.width).toFloat()
                    else if (x < (v.parent as View).x) x = (v.parent as View).x

                    y = event.rawY - yToSub
                    if (y + v.height > (v.parent as View).height) y = ((v.parent as View).height - v.height).toFloat()
                    else if (y < 0) y = 0f

                    v.x = x
                    v.y = y

                }
            }
            return true
        }

    }


    override fun onBackPressed() {
    }
}
