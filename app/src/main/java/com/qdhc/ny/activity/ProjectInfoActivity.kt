package com.qdhc.ny.activity

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.qdhc.ny.R
import com.qdhc.ny.adapter.ProjectSchueduleAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.ProjSchedule
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_contradiction_info.*
import kotlinx.android.synthetic.main.layout_title_theme.*

class ProjectInfoActivity : BaseActivity() {

    lateinit var project: Project

    lateinit var adapter: ProjectSchueduleAdapter

    lateinit var userInfo: UserInfo

    lateinit var projSchedule: ProjSchedule

    var scheduleList = ArrayList<ProjSchedule>()

    override fun intiLayout(): Int {
        return R.layout.activity_project_info
    }

    override fun initView() {
        title_tv_title.text = "工程详情"

        rlv.layoutManager = LinearLayoutManager(this)
        adapter = ProjectSchueduleAdapter(this, scheduleList)
        rlv.adapter = adapter

        adapter.setOnItemClickListener { adapter, v, position ->
            var projSchedule = scheduleList[position]
            var intent = Intent(this, ProjectScheduleInfoActivity::class.java)
            intent.putExtra("project", project)
            intent.putExtra("schedule", projSchedule)
            startActivity(intent)
        }
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }
        bt_comment.setOnClickListener {
            var intent = Intent(this, AddProjScheduleActivity::class.java)
            intent.putExtra("project", project)
            startActivityForResult(intent, 103)
        }
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(this)

        project = intent.getSerializableExtra("info") as Project

        projectTv.text = project.name
        descriptionTv.text = project.introduce

        projSchedule = ProjSchedule()
        projSchedule.content = "工程创建"
        projSchedule.schedule = 0
        projSchedule.pid = project.objectId
        projSchedule.uid = project.manager
        projSchedule.remark = project.createdAt

        getData()

        // 检测是不是需要显示
        try {
            checkBtnShow(project.schedules.get(0).schedule)
        } catch (e: Exception) {
        }

    }

    fun getData() {
        if (project.schedules != null && project.schedules.size > 0) {
            scheduleList.addAll(project.schedules)
        }

        scheduleList.add(projSchedule)
        project.schedules = scheduleList
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 103) {
            if (data != null) {
                val projSchedule = data.getSerializableExtra("schedule")
                scheduleList.add(0, projSchedule as ProjSchedule)
                adapter.notifyDataSetChanged()
                checkBtnShow(projSchedule.schedule)
            }
        }
    }

    fun checkBtnShow(process: Int) {
        if (process == 100) {
            bt_comment.visibility = View.GONE
        }
    }
}
