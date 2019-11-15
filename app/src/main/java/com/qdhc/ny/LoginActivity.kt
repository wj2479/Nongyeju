package com.qdhc.ny

import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.util.Log
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.BaseUtil
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.SharedPreferencesUtil
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_login.*


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity() {
    override fun intiLayout(): Int {
        return R.layout.activity_login
    }

    private var timer: CountDownTimer? = null
    override fun initView() {
        et_username.setText(SharedPreferencesUtil.get(this, "usr"))

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
            showSingleAlertDialog()
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
    }

    lateinit var alertDialog2: AlertDialog

    fun showSingleAlertDialog() {
        val items = arrayOf("上报者1", "上报者2", "东港监理", "莒县监理", "领导")
        val values = arrayOf("guest", "www", "donggang", "juxian", "leader")
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


    //登录
    fun loginByPhone(moblie: String, code: String) {
        var query = BmobQuery<UserInfo>()

        query.addWhereEqualTo("mobilePhoneNumber", moblie)
        query.addWhereEqualTo("password", code)
        query.findObjects(object : FindListener<UserInfo>() {
            override fun done(list: List<UserInfo>?, e: BmobException?) {
                dismissDialogNow()
                if (e == null) {
                    if (list!!.size > 0) {
                        var user = list.get(0)
                        Log.e("TAG", "登录成功:" + user)
                        onLoginSuccess(user, code)
                        finish()
                    } else {
                        ToastUtil.show(mContext, "登录失败,请检查您的输入是否正确")
                    }
                } else {
                    ToastUtil.show(mContext, "登录失败")
                }
                bt_login.isEnabled = true
            }
        })
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

    fun onLoginSuccess(user: UserInfo, pwd: String) {
        SharedPreferencesUtils.saveLogin(mContext, user)
        SharedPreferencesUtil.save(mContext, "usr", user.username)
        SharedPreferencesUtil.save(mContext, "pwd", pwd)

        if (user.role == 3) {
            startActivity(Intent(mContext, Main2Activity::class.java))
        } else {
            startActivity(Intent(mContext, MainActivity::class.java))
        }
    }

}
