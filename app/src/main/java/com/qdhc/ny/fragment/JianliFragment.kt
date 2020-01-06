package com.qdhc.ny.fragment


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.QueryListener
import com.qdhc.ny.activity.CameraActivity
import com.qdhc.ny.activity.ProjectInfoActivity
import com.qdhc.ny.activity.UpdateDailyReportActivity
import com.qdhc.ny.adapter.ProjectPageAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.Area_Region
import com.qdhc.ny.bmob.ProjSchedule
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.fragment_jianli.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * 监理主页面
 */
class JianliFragment : BaseFragment() {

    var projectList = ArrayList<Project>()

    lateinit var userInfo: UserInfo

    var mAdapter = ProjectPageAdapter(context, projectList)

    var project: Project? = null

    var orderList = ArrayList<Project>()

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.fragment_jianli
    }

    override fun initView() {
//        viewPager.setAdapter(mAdapter);
    }

    override fun initClick() {
        cameraIv.setOnClickListener {
            getPermissions()
        }
        projectLayout.setOnClickListener {
            if (project != null) {
                var intent = Intent(context, ProjectInfoActivity::class.java)
                intent.putExtra("info", project)
                startActivity(intent)
            }
        }
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
        helloTv.text = "您好, " + userInfo.nickName
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日")
        val str1 = arrayOf("", "日", "一", "二", "三", "四", "五", "六")
        dateTv.text = "今天是 " + simpleDateFormat.format(Date()) + "\t星期" + str1[calendar.get(Calendar.DAY_OF_WEEK)]

//        tv_all_sort.text = "18"
//        tv_sort.text = "80%"

        getCityArea()
    }

    /**
     * 获取当前城市的信息
     */
    private fun getCityArea() {
        val bmobQuery = BmobQuery<Area_Region>()
        bmobQuery.getObject(userInfo.city, object : QueryListener<Area_Region>() {
            override fun done(region: Area_Region?, e: BmobException?) {
                if (e == null) {
                    if (region != null) {
                        tv_title.text = region.name + "高标准农田建设"
                    }
                }
            }
        })
    }

    /**
     * 获得排序
     */
    private fun getOrderProject() {
        val categoryBmobQuery = BmobQuery<Project>()
        categoryBmobQuery.addWhereEqualTo("city", userInfo.city)
        categoryBmobQuery.order("-schedule")
        categoryBmobQuery.findObjects(
                object : FindListener<Project>() {
                    override fun done(list: List<Project>?, e: BmobException?) {
                        if (e == null) {
                            Log.e("排序列表结果-----》", list?.toString())
                            if (list != null) {
                                orderList.clear()
                                orderList.addAll(list)
                                orderList.forEachIndexed { index, orderPro ->
                                    if (index == 0) {
                                        val project = orderList.get(0)
                                        companyTv.text = project.name
                                    }
                                    if (orderPro.objectId.equals(project!!.objectId)) {
                                        order_city_tv.text = (index + 1).toString()
                                        return@forEachIndexed
                                    }
                                }
                            }
                        } else {
                            Log.e("工程异常-----》", e.toString())
                        }
                    }
                })
    }


    override fun onResume() {
        super.onResume()
        getProjectData()
    }

    val mHandler = Handler()

    override fun lazyLoad() {
//        mHandler.postDelayed(object : Runnable {
//            override fun run() {
//                if (ProjectData.getInstance().location != null) {
//                    HeWeather.getWeatherForecast(context, ProjectData.getInstance().location.city, object : HeWeather.OnResultWeatherForecastBeanListener {
//                        override fun onSuccess(forcast: Forecast?) {
//                            Log.e("TAG", "天气：" + Gson().toJson(forcast))
//
//                            if (Code.OK.getCode().equals(forcast?.getStatus())) {
//                                //此时返回数据
//                                if (forcast?.daily_forecast != null && forcast.daily_forecast.size > 0) {
//                                    var base = forcast.daily_forecast.get(0)
//                                    weatherTv.text = base.tmp_min + " - " + base.tmp_max + "℃"
//                                    weatherInfoTv.text = ProjectData.getInstance().location.district + "    " + base.cond_txt_d + "    \t" + base.wind_dir + "  \t" + base.wind_sc + "级"
//                                }
//                            } else {
//                                //在此查看返回数据失败的原因
//                            }
//
//                        }
//
//                        override fun onError(e: Throwable?) {
//                            Log.e("TAG", "天气异常：" + e.toString())
//                        }
//                    })
//
//                    HeWeather.getWeatherNow(context, ProjectData.getInstance().location.city, object : HeWeather.OnResultWeatherNowBeanListener {
//                        override fun onSuccess(now: Now?) {
//                            if (Code.OK.getCode().equals(now?.getStatus())) {
//                                //此时返回数据
//                                tempTv.text = now?.now?.tmp + "℃"
//                            } else {
//                                //在此查看返回数据失败的原因
//                            }
//                        }
//
//                        override fun onError(e: Throwable?) {
//                        }
//                    })
//                } else {
//                    mHandler.postDelayed(this, 800)
//                }
//            }
//        }, 800)
    }

    // 记录请求的总次数
    var maxCount = 0
    var count = 0

    fun getProjectData() {
        val categoryBmobQuery = BmobQuery<Project>()
        categoryBmobQuery.addWhereEqualTo("manager", userInfo.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(
                object : FindListener<Project>() {
                    override fun done(list: List<Project>?, e: BmobException?) {
                        if (e == null) {
                            Log.e("工程列表结果-----》", list?.toString())
                            projectList.addAll(list!!)
                            maxCount = projectList.size
                            count = 0

                            if (list?.size > 0) {
                                project = list[0]
                                project_name_tv.text = project!!.name
                                getSchedule(project!!)
                                getDistrict(project!!)

                                getOrderProject()
                            } else {
                                project_name_tv.text = "您还没有项目"
                            }
                        } else {
                            Log.e("工程异常-----》", e.toString())
                        }
                    }
                })
    }


    /**
     * 获取项目的进度
     */
    fun getSchedule(project: Project) {
        val categoryBmobQuery = BmobQuery<ProjSchedule>()
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(object : FindListener<ProjSchedule>() {
            override fun done(list: MutableList<ProjSchedule>?, e: BmobException?) {
                Log.e("项目进度-----》", list?.toString())
                if (e == null) {
                    var progress = project.schedule
                    if (list!!.size > 0) {
                        progress = Math.max(project.schedule, list[0].schedule)
                    }
                    progressbar.setProgress(progress)
                    progressTv.text = progress.toString() + "%"
                }
            }
        })
    }

    /**
     * 获取项目的进度
     */
    fun getDistrict(project: Project) {
        val bmobQuery = BmobQuery<Area_Region>()
        bmobQuery.getObject(project.county, object : QueryListener<Area_Region>() {
            override fun done(region: Area_Region?, e: BmobException?) {
                if (e == null) {
                    if (region != null) {
                        tv_district.text = region.name
                    }
                }
            }
        })
    }

    val GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                startActivityForResult(Intent(activity, CameraActivity::class.java), 100);
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(activity!!, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                ), GET_PERMISSION_REQUEST);
            }
        } else {
            startActivityForResult(Intent(activity!!, CameraActivity::class.java), 100);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG", "onActivityResult--> " + resultCode)

        if (resultCode == 101 || resultCode == 102) {
            val intent = Intent(data)
            intent.setClass(activity, UpdateDailyReportActivity::class.java);
            intent.putExtra("code", resultCode)
            intent.putExtra("project", project)
            startActivity(intent)
        } else if (resultCode == 103) {
            Toast.makeText(activity, "请检查相机权限~", Toast.LENGTH_SHORT).show();
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
                    startActivityForResult(Intent(activity, CameraActivity::class.java), 100);
                } else {
                    Toast.makeText(activity, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
