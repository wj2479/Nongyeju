package com.qdhc.ny

import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.util.Log
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import com.google.gson.reflect.TypeToken
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.utils.BaseUtil
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.net.Rx.RxRestClient
import com.sj.core.utils.SharedPreferencesUtil
import com.sj.core.utils.ToastUtil
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity() {
    override fun intiLayout(): Int {
        return R.layout.activity_login
    }

    private var timer: CountDownTimer? = null
    override fun initView() {
        val savedUsername = SharedPreferencesUtil.get(this, "usr")
        if (savedUsername.isNotEmpty()) {
            et_username.setText(savedUsername)
        }

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                login_tv_send.text = "重新获取(" + millisUntilFinished / 1000 + "s)"
                login_tv_send.isClickable = false
            }

            override fun onFinish() {
                login_tv_send.isClickable = true
                login_tv_send.text = "获取验证码"
//                login_tv_send.setBackgroundResource(R.drawable.btn_blue)
                et_username.isEnabled = true
            }
        }
    }

    override fun initClick() {

        logoIv.setOnClickListener {
            //            showSingleAlertDialog()
        }

        bt_login.setOnClickListener {
            val mobile = et_username.text.toString()
            val password = et_password.text.toString()

            if ((mobile).isEmpty()) {
                ToastUtil.show(mContext, "请填写账号!")
                return@setOnClickListener
            }
            if (password.length == 0) {
                ToastUtil.show(mContext, "密码不能为空!")
                return@setOnClickListener
            }
            bt_login.isEnabled = false
            showDialog("正在登录...")
            loginByUserName(mobile, password)
        }
        //发送验证码
        login_tv_send.setOnClickListener {
            val mobile = et_username.text.toString()
            if ((mobile).isEmpty()) {
                ToastUtil.show(mContext, "请填写手机号!")
                return@setOnClickListener
            }
            if (mobile.matches(BaseUtil.MobileRegular.toRegex())) {
//                sendcode(mobile)
            } else {
                ToastUtil.show(mContext, "手机号格式错误，请重新输入!")
                return@setOnClickListener
            }
        }
    }

    override fun initData() {
//        getData()
    }

    fun getData() {
        var jsonObj = JSONObject()
        jsonObj.put("CommandType", "0000")
        jsonObj.put("SQL", "select * from table_user")

        RxRestClient.create()
                .url("XIHE")
                .raw(jsonObj.toString())
                .build()
                .postRaw()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            Log.e("TAG", "登录成功:" + result)
                        },
                        { throwable ->
                            Log.e("TAG", "登录失败:" + throwable.toString())
                        })
    }


    lateinit var alertDialog2: AlertDialog

    fun showSingleAlertDialog() {
        val items = arrayOf("监理1", "南湖", "东港区", "莒县", "市领导")
        val values = arrayOf("nhjl", "nanhu", "donggang", "juxian", "leader3")
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("请选择测试账号")
        alertBuilder.setSingleChoiceItems(items, 0,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    et_username.setText(values.get(i))
                    alertDialog2.dismiss()
                })
        alertDialog2 = alertBuilder.create()
        alertDialog2.show()
    }

    fun loginByUserName(username: String, password: String) {
        var user = UserInfo()
        user.username = username
        user.setPassword(password)
        user.login(object : SaveListener<UserInfo>() {
            override fun done(user: UserInfo?, e: BmobException?) {
                dismissDialogNow()
                if (e == null) {
                    Log.e("TAG", "登录成功:" + user)
                    onLoginSuccess(user!!, password)
                    finish()
                } else {
                    ToastUtil.show(mContext, "登录失败")
                }
                bt_login.isEnabled = true
            }
        })
    }

    fun loginByUserName22(username: String, password: String) {
        var jsonObj = JSONObject()
        jsonObj.put("CommandType", "0000")
        jsonObj.put("SQL", "select * from _user where username='" + username + "' and password = '" + password + "'")

        RxRestClient.create()
                .url("XIHE")
                .raw(jsonObj.toString())
                .build()
                .postRaw()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            Log.e("TAG", "登录成功:" + result)
                            if (result.isNullOrEmpty() || result.equals(" -1")) {
                                ToastUtil.show(mContext, "登录失败,请检查您的输入是否正确")
                            } else {
                                val list: List<UserInfo> = gson.fromJson(result, object : TypeToken<List<UserInfo>>() {}.getType())
                                if (list.size == 1) {
                                    onLoginSuccess(list[0], password)
                                } else {
                                    ToastUtil.show(mContext, "登录失败,请联系管理员")
                                }
                            }
                            dismissDialogNow()
                            bt_login.isEnabled = true
                        },
                        { throwable ->
                            throwable.printStackTrace()
                            ToastUtil.show(mContext, "登录失败,请检查您的网络")
                            dismissDialogNow()
                            bt_login.isEnabled = true
                        })
    }

    fun onLoginSuccess(user: UserInfo, pwd: String) {
        SharedPreferencesUtils.saveLogin(mContext, user)
        SharedPreferencesUtil.save(mContext, "usr", user.username)
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

}
