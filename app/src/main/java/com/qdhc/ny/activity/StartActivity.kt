package com.qdhc.ny.activity

import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.datatype.BmobDate
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.CountListener
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.google.gson.Gson
import com.qdhc.ny.*
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.ContradictPic
import com.qdhc.ny.bmob.DailyReport
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.bmob.Villages
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.SharedPreferencesUtil
import com.tencent.bugly.crashreport.CrashReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class StartActivity : BaseActivity() {

    override fun intiLayout(): Int {
        return R.layout.activity_start
    }

    override fun initView() {
        getHasVillages()
//        getData()
    }

    private fun init() {
        var saveUser = SharedPreferencesUtils.loadLogin(mContext);
        var savePwd = SharedPreferencesUtil.get(mContext, "pwd")
        if (TextUtils.isEmpty(saveUser.objectId) || TextUtils.isEmpty(savePwd)) {
            startActivity(Intent(mContext, LoginActivity::class.java))
            finish()
        } else {
            loginByUserName(saveUser.username, savePwd)
        }
    }


    /**
     * 获取村落列表
     */
    fun getHasVillages() {
        val categoryBmobQuery = BmobQuery<Villages>()
        categoryBmobQuery.findObjects(object : FindListener<Villages>() {
            override fun done(list: List<Villages>?, e: BmobException?) {
                if (e == null) {
                    Log.e("村落列表结果-----》", list.toString())
                    ProjectData.getInstance().villages = list
                    list?.forEach { village -> ProjectData.getInstance().villageMap.put(village.objectId, village) }
                } else {
                    Log.e("异常-----》", e.toString())
                }
            }
        })
    }

    var map = TreeMap<String, Int>();
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    fun getData() {
        val categoryBmobQuery = BmobQuery<UserInfo>()
        categoryBmobQuery.addWhereEqualTo("role", 1)
        categoryBmobQuery.addWhereEqualTo("city", "hARs111J")
        categoryBmobQuery.findObjects(object : FindListener<UserInfo>() {
            override fun done(list: List<UserInfo>?, e: BmobException?) {
                if (e == null) {
                    if (list != null) {
                        list.forEach { user ->
                            val categoryBmobQuery = BmobQuery<DailyReport>()
                            categoryBmobQuery.addWhereEqualTo("uid", user.objectId);
                            var createdAtStart = "2019-11-01 00:00:00";
                            var createdAtDateStart = sdf.parse(createdAtStart);
                            var bmobCreatedAtDateStart = BmobDate(createdAtDateStart);

                            var createdAtEnd = "2020-01-01 00:00:00";
                            var createdAtDateEnd = sdf.parse(createdAtEnd);
                            var bmobCreatedAtDateEnd = BmobDate(createdAtDateEnd);

                            categoryBmobQuery.addWhereGreaterThanOrEqualTo("createdAt", bmobCreatedAtDateStart);
                            categoryBmobQuery.addWhereLessThanOrEqualTo("createdAt", bmobCreatedAtDateEnd);

                            categoryBmobQuery.findObjects(object : FindListener<DailyReport>() {
                                override fun done(list: MutableList<DailyReport>?, e: BmobException?) {
                                    if (e == null) {
                                        if (list != null) {
                                            list.forEach { report ->
                                                val categoryBmobQuery = BmobQuery<ContradictPic>()
                                                categoryBmobQuery.addWhereEqualTo("contradict", report.objectId)
                                                categoryBmobQuery.count(ContradictPic::class.java, object : CountListener() {
                                                    override fun done(count: Int?, e: BmobException?) {
                                                        if (e == null && count != null) {
//                                                            Log.e("TAG", user.objectId + " " + user.nickName + " " + report.objectId + "  " + report.createdAt.substring(0, 10) + " 计数:" + count.toString())
                                                            var key = user.nickName + "," + report.createdAt.substring(0, 10)

                                                            if (map.containsKey(key)) {
                                                                map.put(key, count + map.get(key)!!)
                                                            } else {
                                                                map.put(key, count)
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        })

        Handler().postDelayed({
            var ss = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "data.txt";

            var file = File(ss)
            if (file.exists()) {
                file.delete()
            }

            file.createNewFile()

            var fos = FileOutputStream(file)

            var title = "name,date,count\n"
            fos.write(title.toByteArray())

            map.entries.forEach { entry ->
                var data = entry.key + "," + entry.value.toString() + "\n"
                fos.write(data.toByteArray())
                fos.flush()
                Log.e("TAG", data)
            }
            fos.close()

        }, 5000)
    }


    fun loginByUserName(username: String, password: String) {
        var user = UserInfo()
        user.username = username
        user.setPassword(password)
        user.login(object : SaveListener<UserInfo>() {
            override fun done(user: UserInfo?, e: BmobException?) {
                if (e == null) {
                    Log.e("TAG", "自动登录成功:" + Gson().toJson(user))
                    onLoginSuccess(user!!, password)
                } else {
                    startActivity(Intent(mContext, LoginActivity::class.java))
                }
                finish()
            }
        })
    }

    fun onLoginSuccess(user: UserInfo, pwd: String) {
        SharedPreferencesUtils.saveLogin(mContext, user)
        SharedPreferencesUtil.save(mContext, "pwd", pwd)
        ProjectData.getInstance().userInfo = user

        CrashReport.setUserId(user.username)

        when (user.role) {
            1 -> startActivity(Intent(mContext, MainActivity::class.java))
            2 -> startActivity(Intent(mContext, Main2Activity::class.java))
            3 -> startActivity(Intent(mContext, Main3Activity::class.java))
            4 -> startActivity(Intent(mContext, Main4Activity::class.java))
        }
    }

    override fun initClick() {
    }

    var flag = true
    private val TIMER = 999
    lateinit var thread: Thread
    override fun initData() {
        thread = Thread(Runnable {
            if (flag) {
                try {
                    Thread.sleep(2000) //休眠
                    mHanler.sendEmptyMessage(TIMER)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
        thread.start()
    }

    private val mHanler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                TIMER -> {
                    init()
                    flag = false
                }
                else -> {
                }
            }//去执行定时操作逻辑
        }
    }

    private fun stopTimer() {
        flag = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        mHanler.removeCallbacks(thread)
    }

}
