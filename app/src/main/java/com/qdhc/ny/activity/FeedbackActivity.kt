package com.qdhc.ny.activity

import android.support.v4.content.ContextCompat
import android.view.View
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import com.qdhc.ny.R
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.Feedback
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.layout_title_theme.*

/**
 * 反馈意见
 * @author shenjian
 * @date 2019/3/10
 */
class FeedbackActivity : BaseActivity() {

    lateinit var userInfo: UserInfo
    override fun intiLayout(): Int {
        return (R.layout.activity_feedback)
    }

    override fun initView() {
        title_tv_title.text = "意见反馈"
        title_tv_right.text = "发送"
        title_tv_right.visibility = View.VISIBLE
        title_tv_right.setTextColor(ContextCompat.getColor(mContext, R.color.themecolor))
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }
        title_tv_right.setOnClickListener {
            if (edt_content.text.length > 6) {
                upData(edt_content.text.toString())
            } else {
                ToastUtil.show(mContext, "您输入的内容有点少")
            }
        }
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(this)
    }

    /***
     * 上传数据
     */
    fun upData(content: String) {
        var feedback = Feedback()
        feedback.content = content
        feedback.userId = userInfo.objectId
        feedback.save(object : SaveListener<String>() {
            override fun done(objectId: String?, e: BmobException?) {
                if (e == null) {
                    ToastUtil.show(mContext, "反馈成功");
                    finish()
                } else {
                    ToastUtil.show(mContext, "反馈失败");
                }
            }
        })
    }

}
