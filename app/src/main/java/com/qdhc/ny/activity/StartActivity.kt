package com.qdhc.ny.activity

import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.google.gson.Gson
import com.qdhc.ny.*
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.bmob.Villages
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.SharedPreferencesUtil
import com.tencent.bugly.crashreport.CrashReport

class StartActivity : BaseActivity() {

    override fun intiLayout(): Int {
        return R.layout.activity_start
    }

    override fun initView() {
        getHasVillages()
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

    fun loginByPhone(moblie: String, code: String) {
        var query = BmobQuery<UserInfo>()

        query.addWhereEqualTo("mobilePhoneNumber", moblie)
        query.addWhereEqualTo("password", code)
        query.findObjects(object : FindListener<UserInfo>() {
            override fun done(list: List<UserInfo>?, e: BmobException?) {
                if (e == null) {
                    if (list!!.size > 0) {
                        var user = list.get(0)
                        Log.e("TAG", "自动登录成功:" + Gson().toJson(user))
                        SharedPreferencesUtils.saveLogin(mContext, user)

                        when (user.role) {
                            1 -> startActivity(Intent(mContext, MainActivity::class.java))
                            2 -> startActivity(Intent(mContext, Main4Activity::class.java))
                            3 -> startActivity(Intent(mContext, Main2Activity::class.java))
                            4 -> startActivity(Intent(mContext, Main4Activity::class.java))
                        }
                    } else {
                        startActivity(Intent(mContext, LoginActivity::class.java))
                    }
                } else {
                    startActivity(Intent(mContext, LoginActivity::class.java))
                }
                finish()
            }
        })
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
