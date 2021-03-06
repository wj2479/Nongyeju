package com.qdhc.ny

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Toast
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.SaveListener
import com.amap.api.location.*
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.google.gson.Gson
import com.luck.picture.lib.permissions.RxPermissions
import com.qdhc.ny.activity.CameraActivity
import com.qdhc.ny.activity.NotifyDetailActivity
import com.qdhc.ny.adapter.TabFragmentPagerAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.base.BaseApplication
import com.qdhc.ny.bean.TabIconBean
import com.qdhc.ny.bmob.*
import com.qdhc.ny.common.Constant
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.eventbus.ProjectEvent
import com.qdhc.ny.fragment.JianliFragment
import com.qdhc.ny.fragment.MyFragment
import com.qdhc.ny.fragment.NotifyFragment
import com.qdhc.ny.service.UpadateManager
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.SharedPreferencesUtil
import com.sj.core.utils.ToastUtil
import com.vondear.rxui.view.dialog.RxDialogSureCancel
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity() {

    var gson = Gson()

    val GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    // 记录上一次上传的位置信息
    var lastUploadLocation: AMapLocation? = null
    // 上传数据之间最小的距离
    val MIN_DISTANCE = 200
    // 允许的最大精度误差
    val MAX_ACCURACY = 80

    lateinit var userInfo: UserInfo

    override fun intiLayout(): Int {
        return (R.layout.activity_main)
    }

    override fun initClick() {
    }

    override fun initData() {
        UpadateManager.checkVersion(this)
        getNotifyData()
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        var rxPermission = RxPermissions(this)

        rxPermission.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        ).subscribe({ permission ->
            if (permission.granted) {
                // 用户已经同意该权限
                Log.d("", permission.name + " is granted.")
                if (permission.name.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                }
            } else if (permission.shouldShowRequestPermissionRationale) {
                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
                ToastUtil.show(mContext, "拒绝了该权限，没有选中『不再询问』")

                Log.d("", permission.name + " is denied. More info should be provided.")
            } else {
                // 用户拒绝了该权限，而且选中『不再询问』
                ToastUtil.show(mContext, "拒绝了该权限，而且选中『不再询问』")

                Log.d("", permission.name + " is denied.")
            }

        })
    }

    //UI
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mIconUnselectIds = intArrayOf(R.drawable.ic_list,
            R.mipmap.icon_notice,
            R.mipmap.icon_wode)
    private val mIconSelectIds = intArrayOf(R.drawable.ic_list_select,
            R.mipmap.icon_notice_select,
            R.mipmap.icon_wode_select)

    var projectList = ArrayList<Project>()

    override fun initView() {
        userInfo = SharedPreferencesUtils.loadLogin(this)

        //获取数据 在values/arrays.xml中进行定义然后调用
        var tabTitle = resources.getStringArray(R.array.tab_titles)
        //将fragment装进列表中
        var fragmentList = ArrayList<Fragment>()
        fragmentList.add(JianliFragment())
        fragmentList.add(NotifyFragment())
        fragmentList.add(MyFragment())
        //viewpager加载adapter
        vp.adapter = TabFragmentPagerAdapter(supportFragmentManager, fragmentList, tabTitle)
        for (i in fragmentList.indices) {
            mTabEntities.add(TabIconBean(tabTitle[i], mIconSelectIds[i], mIconUnselectIds[i]))
        }
        tl.setTabData(mTabEntities)
        initPager()
        if (SharedPreferencesUtil.get(mContext, Constant.NOTICE) != "true") {
            initDialog()
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initLocation()
        locationClient.disableBackgroundLocation(true)
        if (Build.VERSION.SDK_INT >= 26) {
            locationClient.enableBackgroundLocation(2001, buildNotification())
        }
    }

    private fun initDialog() {
        var rxDialogSureCancel = RxDialogSureCancel(mContext)
        rxDialogSureCancel.contentView.text = Html.fromHtml("当手机在锁屏时，部分手机会与定位断开连接<br>例如：华为<br>1、 进入\"设置”，点击\"高级设置\"； <br>2、 点击\"电池管理-受保护应用\"； <br>3、 将本APP设置开关开启。<br>例如：小米<br>1、 进入\"设置\"，点击\"电量和性<br>2、 点击\"神隐模式-应用配置\"；<br> 3、 将本APP应用，设置为\"无限制\"")
        rxDialogSureCancel.contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
        rxDialogSureCancel.contentView.gravity = Gravity.LEFT
        rxDialogSureCancel.titleView.text = "温馨提示"
        rxDialogSureCancel.titleView.textSize = 16.0f
        rxDialogSureCancel.setSure("确定")
        rxDialogSureCancel.sureView.setOnClickListener {
            rxDialogSureCancel.cancel()
        }
        rxDialogSureCancel.setCancel("不再提示")
        rxDialogSureCancel.cancelView.setOnClickListener {
            rxDialogSureCancel.cancel()
            SharedPreferencesUtil.save(mContext, Constant.NOTICE, true.toString())
        }
        rxDialogSureCancel.show()
    }

    /**
     * 获取通知数据
     */
    fun getNotifyData() {
        val categoryBmobQuery = BmobQuery<NotifyReceiver>()
        categoryBmobQuery.addWhereEqualTo("uid", userInfo.objectId)
        categoryBmobQuery.addWhereEqualTo("isRead", false)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.setLimit(1)
        categoryBmobQuery.findObjects(
                object : FindListener<NotifyReceiver>() {
                    override fun done(list: List<NotifyReceiver>, e: BmobException?) {
                        if (e == null) {
                            if (list.size > 0) {
                                var notifyReceiver = list[0]
                                val categoryBmobQuery = BmobQuery<Notify>()
                                categoryBmobQuery.getObject(notifyReceiver.nid, object : QueryListener<Notify>() {
                                    override fun done(notify: Notify, e: BmobException?) {
                                        initDialog(notify, notifyReceiver)
                                    }
                                })
                            }
                        } else {
                            Log.e("异常-----》", e.toString())
                        }
                    }
                })
    }

    /**
     * 初始化通知的对话框
     */
    private fun initDialog(notify: Notify, notifyReceiver: NotifyReceiver) {
        var sb = StringBuffer()
        sb.append(notify.content.replace("\n", "<br>"))
//        sb.append("<br>")
//
//        sb.append("<font color=\"#808080\">")
//        sb.append("发布于: ")
//        sb.append(notify.createdAt.substring(0, 10))
//        sb.append("</font>")

        var rxDialogSureCancel = RxDialogSureCancel(mContext)
        rxDialogSureCancel.contentView.text = Html.fromHtml(sb.toString())
        rxDialogSureCancel.contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
        rxDialogSureCancel.contentView.gravity = Gravity.LEFT
        rxDialogSureCancel.titleView.text = "您有新的通知"
        rxDialogSureCancel.titleView.textSize = 16.0f
        rxDialogSureCancel.setSure("知道了")
        rxDialogSureCancel.sureView.setOnClickListener {
            rxDialogSureCancel.cancel()
        }
        rxDialogSureCancel.setCancel("查看详情")
        rxDialogSureCancel.cancelView.setOnClickListener {
            rxDialogSureCancel.cancel()
            var intent = Intent(this, NotifyDetailActivity::class.java)
            intent.putExtra("notify", notify)
            intent.putExtra("notifyReceiver", notifyReceiver)
            startActivity(intent)
        }
        rxDialogSureCancel.show()
    }

    var tab_position = 0
    override fun onRestart() {
        super.onRestart()
        tl.currentTab = tab_position
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG", "onActivityResult--> " + resultCode)

//        if (resultCode == 101 || resultCode == 102) {
//            val intent = Intent(data)
//            intent.setClass(this@MainActivity, UpdateDailyReportActivity::class.java);
//            intent.putExtra("code", resultCode)
//            intent.putExtra("project", projectList.get(0))
//            startActivity(intent)
//        } else if (resultCode == 103) {
//            Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show();
//        }

    }

    private fun initPager() {
        tl.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
//                if (position == 2) {
////                    startActivityForResult(Intent(this@MainActivity, SignInActivity::class.java), 1)
//                    if (projectList.size > 0) {
//                        getPermissions()
//                    } else {
//                        ToastUtil.show(this@MainActivity, "您还没有负责的项目")
//                    }
//                } else {
                vp.currentItem = position
                tab_position = position
//                }
            }

            override fun onTabReselect(position: Int) {
                if (position == 2) {
//                    startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                }
            }
        })

        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position != 2) {
                    tl.currentTab = position
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        //默认选中第一个
        vp.currentItem = 0
        vp.offscreenPageLimit = 5
        vp.setNoScroll(true)
    }

    /**
     * 获取权限
     */
    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                startActivityForResult(Intent(this, CameraActivity::class.java), 100);
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                ), GET_PERMISSION_REQUEST);
            }
        } else {
            startActivityForResult(Intent(this, CameraActivity::class.java), 100);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GET_PERMISSION_REQUEST) {
            var size = 0;
            if (grantResults.size >= 1) {
                var writeResult = grantResults[0];
                //读写内存权限
                var writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                var recordPermissionResult = grantResults[1];
                var recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                var cameraPermissionResult = grantResults[2];
                var cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    startActivityForResult(Intent(this, CameraActivity::class.java), 100);
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        locationClient.startLocation()
        //切入前台后关闭后台定位功能
        if (null != locationClient) {
            locationClient.disableBackgroundLocation(true)
        }
//        getProjectData()
    }

    // 记录请求的总次数
    var maxCount = 0
    var count = 0

    //获取数据
    fun getProjectData() {
        val categoryBmobQuery = BmobQuery<Project>()
        categoryBmobQuery.addWhereEqualTo("manager", userInfo.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(
                object : FindListener<Project>() {
                    override fun done(list: List<Project>?, e: BmobException?) {
                        if (e == null) {
                            projectList.clear()
                            projectList.addAll(list!!)
                            maxCount = projectList.size
                            count = 0
                            projectList.forEach { project ->
                                getSchedule(project)
//                                getReports(project)
                            }
                        } else {
                            Log.e("异常-----》", e.toString())
                        }
                    }
                })
    }

    /**
     * 获取项目的进度
     */
    fun getSchedule(project: Project) {
        val categoryBmobQuery = BmobQuery<ProjSchedule>()
        categoryBmobQuery.addWhereEqualTo("uid", userInfo.objectId)
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(object : FindListener<ProjSchedule>() {
            override fun done(list: MutableList<ProjSchedule>?, e: BmobException?) {
                if (e == null) {
                    if (list != null && list.size > 0) {
                        var progress = Math.max(project.schedule, list[0].schedule)
                        project.schedule = progress
                    }
                }
                // 每完成一次  计数+1
                count++
                verifyRequestOver()
            }
        })
    }

    /**
     * 获取项目的日报 周报 月报
     */
    fun getReports(project: Project) {
        val categoryBmobQuery = BmobQuery<Report>()
        categoryBmobQuery.addWhereEqualTo("uid", userInfo.objectId)
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(object : FindListener<Report>() {
            override fun done(list: MutableList<Report>?, e: BmobException?) {
                if (e == null) {
                    var dayList = ArrayList<Report>()
                    var weekList = ArrayList<Report>()
                    var monthList = ArrayList<Report>()

                    if (list != null) {
                        list.forEach { report ->
                            when (report.type) {
                                1 -> dayList.add(report)
                                2 -> weekList.add(report)
                                3 -> monthList.add(report)
                            }
                        }
//                        project.dayRreports = dayList
//                        project.weekRreports = weekList
//                        project.monthRreports = monthList
                    }
                }
                // 每完成一次  计数+1
                count++
                verifyRequestOver()
            }
        })
    }

    fun verifyRequestOver() {
        if (maxCount == count) {
            ProjectData.getInstance().projects = projectList
            Log.e("TAG", "结果--> " + projectList)
            EventBus.getDefault().post(ProjectEvent())
        }
    }

    override fun onStop() {
        super.onStop()
        val isBackground = (application as BaseApplication).isBackground
        //如果app已经切入到后台，启动后台定位功能
//        Log.e("location_main",""+isBackground)
//        if (isBackground) {
//            if (null != locationClient) {
//                Log.e("location_main1","11111111")
//                locationClient.enableBackgroundLocation(2001, buildNotification())
//            }
//        }
        if (Build.VERSION.SDK_INT >= 26) {
            locationClient.enableBackgroundLocation(2001, buildNotification())
        }
    }

    lateinit var locationClient: AMapLocationClient
    lateinit var locationOption: AMapLocationClientOption

    fun initLocation() {
        //初始化client
        locationClient = AMapLocationClient(this.applicationContext)
        locationOption = getDefaultOption()
        //设置定位参数
        locationClient.setLocationOption(locationOption)
        // 设置定位监听
        locationClient.setLocationListener(locationListener)
        startLocation()
    }

    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private fun getDefaultOption(): AMapLocationClientOption {
        var mOption = AMapLocationClientOption()
        //Battery_Saving
//        低功耗模式
//        Device_Sensors
//        仅设备模式,不支持室内环境的定位
//        Hight_Accuracy
//        高精度模式
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        mOption.httpTimeOut = 10000//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        //   mOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        mOption.interval = 1000 * 10//可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true//可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocation = false//可选，设置是否单次定位。默认是false
        mOption.isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.geoLanguage = AMapLocationClientOption.GeoLanguage.DEFAULT//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        mOption.isLocationCacheEnable = false
        return mOption
    }

    /**
     * 定位监听
     */
    var locationListener = AMapLocationListener { location ->

        if (null != location) {
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (location.errorCode == 0) {
                if (location.accuracy > MAX_ACCURACY) {
                    Log.e("AMAP", "定位精度误差:" + location.accuracy + "米")
                    return@AMapLocationListener
                }

                if (lastUploadLocation != null) {
                    var lastLatLng = LatLng(lastUploadLocation!!.latitude, lastUploadLocation!!.longitude)
                    var latLng = LatLng(location.latitude, location.longitude)
                    var distance = AMapUtils.calculateLineDistance(lastLatLng, latLng)

                    var bearingto = location.bearingTo(lastUploadLocation)
                    if (distance < MIN_DISTANCE && bearingto < 60) {   // 判断两次之间的距离  并且夹角小于60度
                        Log.e("AMAP", "两次定位之间距离:" + distance + "米,夹角:" + bearingto)
                        return@AMapLocationListener
                    }
                }

                var tracks = Tracks()
                tracks.deviceFacturer = Build.MANUFACTURER  // 设备厂商
                tracks.deviceModel = Build.MODEL     // 设备型号
                tracks.deviceVersion = Build.VERSION.RELEASE   // 系统版本

                tracks.direction = location.bearing         // 运动方向
                tracks.accuracy = location.accuracy         //精    度
                tracks.lat = location.latitude              // 纬度
                tracks.lng = location.longitude             // 经度
                tracks.locationTime = location.time        // 定位时间
                tracks.speed = location.speed               // 速度
                tracks.uid = userInfo.objectId              // 上传人

                var sb_temp = StringBuffer()
                sb_temp.append("* WIFI开关：").append(if (location.locationQualityReport.isWifiAble) "开启" else "关闭").append("\n");

                when (location.locationType) {
                    AMapLocation.LOCATION_TYPE_GPS -> {
                        sb_temp.append("* 定位类型：").append("GPS").append("\n");
                        sb_temp.append("* GPS状态：").append(getGPSStatusString(location.locationQualityReport.gpsStatus)).append("\n");
                        sb_temp.append("* GPS星数：").append(location.locationQualityReport.gpsSatellites).append("\n");
                        tracks.locationType = "GPS"
                    }
                    AMapLocation.LOCATION_TYPE_CELL -> {
                        tracks.locationType = "网络基站定位"
                    }
                    AMapLocation.LOCATION_TYPE_FIX_CACHE -> {
                        tracks.locationType = "缓存定位"
                    }
                    AMapLocation.LOCATION_TYPE_LAST_LOCATION_CACHE -> {
                        tracks.locationType = "最后位置缓存"
                    }
                    AMapLocation.LOCATION_TYPE_OFFLINE -> {
                        tracks.locationType = "离线定位"
                    }
                    AMapLocation.LOCATION_TYPE_WIFI -> {
                        tracks.locationType = "Wifi定位"
                    }
                }
                tracks.remark = sb_temp.toString()     // 备注信息

                tracks.save(object : SaveListener<String>() {
                    override fun done(objectId: String?, e: BmobException?) {
                        if (e == null) {
                            lastUploadLocation = location
                            Log.e("AMAP", "轨迹上传成功")
                        } else {
                            Log.e("AMAP", "轨迹上传失败:" + e.toString())
                        }
                    }
                })
                ProjectData.getInstance().location = location
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
//            var intent =  Intent()
//            intent.action = "android.intent.action.MAIN"
//            //intent.addCategory("android.intent.category.HOME")
//            startActivity(intent)
            false
        } else {
            super.dispatchKeyEvent(event)
        }

    }

    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private fun getGPSStatusString(statusCode: Int): String {
        var str = ""
        when (statusCode) {
            AMapLocationQualityReport.GPS_STATUS_OK ->
                str = "GPS状态正常"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER ->
                str = "手机中没有GPS Provider，无法进行GPS定位"
            AMapLocationQualityReport.GPS_STATUS_OFF ->
                str = "GPS关闭，建议开启GPS，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING ->
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION ->
                str = "没有GPS定位权限，建议开启gps定位权限"
        }
        return str;
    }


    /**
     * 开始定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun startLocation() {
        // 设置定位参数
        locationClient.setLocationOption(locationOption)
        // 启动定位
        locationClient.startLocation()
    }

    private val NOTIFICATION_CHANNEL_NAME = "BackgroundLocation"
    lateinit var notificationManager: NotificationManager
    var isCreateChannel = false
    @SuppressLint("NewApi", "WrongConstant")
    @TargetApi(Build.VERSION_CODES.O)
    private fun buildNotification(): Notification {

        var builder: NotificationCompat.Builder
        var notification: Notification
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
//            if (null == notificationManager) {
//                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            }
            var channelId = packageName
            if (!isCreateChannel) {
                var notificationChannel = NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.enableLights(true)//是否在桌面icon右上角展示小圆点
                notificationChannel.lightColor = Color.BLUE //小圆点颜色
                notificationChannel.setShowBadge(true)//是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel)
                isCreateChannel = true
            }
            builder = NotificationCompat.Builder(applicationContext, channelId)
        } else {
            builder = NotificationCompat.Builder(applicationContext)
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("工程监理系统")
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis())

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build()
        } else {
            return builder.build()
        }
        return notification
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun stopLocation() {
        // 停止定位
        locationClient.stopLocation()
    }

    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy()
//            locationClient = null
//            locationOption = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyLocation()
    }
}
