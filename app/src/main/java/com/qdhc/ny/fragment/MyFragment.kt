package com.qdhc.ny.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.bmob.v3.BmobUser
import com.qdhc.ny.LoginActivity
import com.qdhc.ny.R
import com.qdhc.ny.activity.FeedbackActivity
import com.qdhc.ny.activity.UpdatePwdActivity
import com.qdhc.ny.activity.UserInfoActivity
import com.qdhc.ny.activity.UserManagerActivity
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.BaseUtil
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.AcitityManagerUtil
import kotlinx.android.synthetic.main.fragment_my.*

class MyFragment : BaseFragment() {

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): MyFragment {
            val fragment = MyFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun intiLayout(): Int {
        return R.layout.fragment_my
    }

    lateinit var userInfo: UserInfo
    override fun initView() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
        tv_name.text = userInfo.nickName

        versionTv.text = BaseUtil.getAppVersionName(context)
        if (userInfo.role == 0) {
            ll_usermanager.visibility = View.VISIBLE
        }
    }

    override fun initClick() {

        btn_exit.setOnClickListener {
            //退出
            upData()
        }
        ll_user.setOnClickListener {
            //更新个人信息
            startActivityForResult(Intent(activity, UserInfoActivity::class.java)
                    .putExtra("user", userInfo), 1)
        }
        ll_feedback.setOnClickListener {
            //意见反馈
            startActivity(Intent(activity, FeedbackActivity::class.java))
        }
        ll_name_vertify.setOnClickListener {
            //修改密码
            startActivity(Intent(activity, UpdatePwdActivity::class.java))
        }

        ll_usermanager.setOnClickListener {
            //用户管理
            startActivity(Intent(activity, UserManagerActivity::class.java))
        }
    }

    override fun initData() {
        when (userInfo.role) {
            0 -> tv_job.text = "管理员"
            1 -> tv_job.text = "上报员"
            2 -> tv_job.text = "监理      管辖区域：" + userInfo.district
            3 -> tv_job.text = "分管领导"
        }
    }

    override fun lazyLoad() {
    }

    /***
     * 退出
     */
    fun upData() {
        BmobUser.logOut();
        startActivity(Intent(activity, LoginActivity::class.java))
        AcitityManagerUtil.getInstance().finishAllActivity()
        SharedPreferencesUtils.removeLogin(context)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            getDatas()
        }
    }


}
