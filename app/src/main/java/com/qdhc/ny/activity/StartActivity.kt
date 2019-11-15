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
import com.qdhc.ny.LoginActivity
import com.qdhc.ny.Main2Activity
import com.qdhc.ny.MainActivity
import com.qdhc.ny.R
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.SharedPreferencesUtil

class StartActivity : BaseActivity() {

    override fun intiLayout(): Int {
        return R.layout.activity_start
    }

    override fun initView() {

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

                        if (user.role == 3) {
                            startActivity(Intent(mContext, Main2Activity::class.java))
                        } else {
                            startActivity(Intent(mContext, MainActivity::class.java))
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

        if (user.role == 3) {
            startActivity(Intent(mContext, Main2Activity::class.java))
        } else {
            startActivity(Intent(mContext, MainActivity::class.java))
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
