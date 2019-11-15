package com.qdhc.ny.activity

import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.qdhc.ny.R
import com.qdhc.ny.adapter.ImageAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.ContradictPic
import com.qdhc.ny.bmob.ProjSchedule
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_project_schedule_info.*
import kotlinx.android.synthetic.main.layout_title_theme.*

class ProjectScheduleInfoActivity : BaseActivity() {

    lateinit var project: Project

    lateinit var adapter: ImageAdapter

    lateinit var userInfo: UserInfo

    lateinit var projSchedule: ProjSchedule

    var selectList = ArrayList<ContradictPic>()

    override fun intiLayout(): Int {
        return R.layout.activity_project_schedule_info
    }

    override fun initView() {
        title_tv_title.text = "工程进度详情"

        rlv.isNestedScrollingEnabled = false
        rlv.layoutManager = GridLayoutManager(this, 4) as RecyclerView.LayoutManager?
        adapter = ImageAdapter(this, selectList)
        adapter.setOnItemClickListener { position, v ->
            var url = selectList.get(position).file.url

            var intent = Intent(this, ImageActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }
        rlv.adapter = adapter
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }

    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(this)

        project = intent.getSerializableExtra("project") as Project
        projSchedule = intent.getSerializableExtra("schedule") as ProjSchedule

        nameTv.text = project.name
        processTv.text = projSchedule.schedule.toString() + "%"
        if (TextUtils.isEmpty(projSchedule.createdAt)) {
            timeTv.text = projSchedule.remark
        } else {
            timeTv.text = projSchedule.createdAt
        }
        contentTv.text = projSchedule.content

        getImags(projSchedule)
    }

    fun getImags(projSchedule: ProjSchedule) {

        if (projSchedule == null || projSchedule.objectId == null) {
            photoLayout.visibility = View.GONE
            return
        }

        var query = BmobQuery<ContradictPic>()
        query.addWhereEqualTo("contradict", projSchedule.objectId)
        query.findObjects(object : FindListener<ContradictPic>() {
            override fun done(`list`: List<ContradictPic>, e: BmobException?) {
                if (e == null) {
                    Log.e("TAG", "获取照片->" + list.size.toString())
                    if (list.size > 0) {
                        selectList.clear()
                        selectList.addAll(list)
                        adapter.notifyDataSetChanged()
                    } else {
                        photoLayout.visibility = View.GONE
                    }
                } else {
                    Log.e("TAG", "获取照片失败:->" + e.toString())
                    ToastUtil.show(this@ProjectScheduleInfoActivity, "获取照片失败")
                    photoLayout.visibility = View.GONE
                }
            }
        })
    }
}
