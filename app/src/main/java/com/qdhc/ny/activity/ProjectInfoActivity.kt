package com.qdhc.ny.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.lcodecore.ILabel
import com.qdhc.ny.R
import com.qdhc.ny.adapter.ProjectSchueduleAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.TagLabel
import com.qdhc.ny.bmob.ProjSchedule
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.ProjectMonth
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.ToastUtil
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_contradiction_info.bt_comment
import kotlinx.android.synthetic.main.activity_contradiction_info.commentButLayout
import kotlinx.android.synthetic.main.activity_contradiction_info.descriptionTv
import kotlinx.android.synthetic.main.activity_contradiction_info.projectTv
import kotlinx.android.synthetic.main.activity_contradiction_info.rlv
import kotlinx.android.synthetic.main.activity_project_info.*
import kotlinx.android.synthetic.main.layout_title_theme.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * 工程详情
 */
class ProjectInfoActivity : BaseActivity() {

    lateinit var project: Project

    lateinit var adapter: ProjectSchueduleAdapter

    lateinit var userInfo: UserInfo

    lateinit var projSchedule: ProjSchedule

    var scheduleList = ArrayList<ProjSchedule>()

    var initSchedule = 0

    val calendar = Calendar.getInstance()

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
            var intent = Intent(this, UpdateDailyReportActivity::class.java)
            intent.putExtra("project", project)
            startActivityForResult(intent, 103)
        }

        bt_report.setOnClickListener {
            var intent = Intent(this, ReportAllListActivity::class.java)
            intent.putExtra("user", userInfo)
            intent.putExtra("project", project)
            startActivity(intent)
        }

        targetTv.setOnClickListener {
            val target = targetTv.text.toString().trim()
            if (target.isNullOrEmpty()) {
                initDialog()
            } else {
                ToastUtil.show(this@ProjectInfoActivity, "本月的目标已经设置完毕，请下个月在来设置")
            }
        }
    }

    private fun initDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_set_target, null);
        val bubbleSeekbar = view.findViewById<BubbleSeekBar>(R.id.bubbleSeekbar)
        bubbleSeekbar.setProgress(initSchedule * 1.0f)
        bubbleSeekbar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                if (progress < initSchedule) {
                    bubbleSeekbar.setProgress(initSchedule * 1.0f)
                }
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
            }
        }
        val builder = AlertDialog.Builder(this)
                .setView(view)
                .setTitle("请设置本月的目标进度")
                .setPositiveButton("确 定", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        var target = bubbleSeekbar.progress
                        if (target > initSchedule) {
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1
                            var projectMonth = ProjectMonth()
                            projectMonth.target = target
                            projectMonth.month = month
                            projectMonth.year = year
                            projectMonth.uid = userInfo.objectId
                            projectMonth.pid = project.objectId
                            projectMonth.save(object : SaveListener<String>() {
                                override fun done(objectId: String?, e: BmobException?) {
                                    if (e == null) {
                                        targetTv.text = target.toString() + "%"
                                    } else {
                                        ToastUtil.show(this@ProjectInfoActivity, "目标设置出错，请重新设置")
                                    }
                                }
                            })
                        }
                    }
                });

        builder.create().show();
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(this)

        project = intent.getSerializableExtra("info") as Project

        projectTv.text = project.name
        descriptionTv.text = project.introduce

        var villageName = ProjectData.getInstance().villageMap.get(project.village)?.name

        if (villageName.isNullOrEmpty())
            villageName = ""

        var mTitles = arrayOf("东港区", "莒县", "五莲县", "岚山区")
        areaTv.text = mTitles[project.area - 1] + "   " + villageName

        if (null != project.tags) {
            var split = project.tags.trim().split(",")

            // 标签的数据
            var labels = ArrayList<ILabel>()
            split.forEach { text ->
                if (!text.trim().isEmpty())
                    labels.add(TagLabel(text, text))
            }

            if (labels.size == 0) {
                labels.add(TagLabel("无标签", "无标签"))
            }

            label_me.setLabels(labels)
        }

        projSchedule = ProjSchedule()
        projSchedule.content = "工程创建"
        projSchedule.schedule = 0
        projSchedule.pid = project.objectId
        projSchedule.uid = project.manager
        projSchedule.remark = project.createdAt



        getData()

        // 检测是不是需要显示
        checkBtnShow(project.schedule)
        scheduleTv.text = project.schedule.toString() + "%"

        if (userInfo.role > 1) {
            commentButLayout.visibility = View.GONE
        }
        getSchedule(project)
    }

    fun getData() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        // 获取当月目标进度
        val categoryBmobQuery = BmobQuery<ProjectMonth>()
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.addWhereEqualTo("year", year)
        categoryBmobQuery.addWhereEqualTo("month", month)
        categoryBmobQuery.order("-target")
        categoryBmobQuery.setLimit(1)
        categoryBmobQuery.findObjects(
                object : FindListener<ProjectMonth>() {
                    override fun done(list: List<ProjectMonth>?, e: BmobException?) {
                        if (e == null) {
                            if (list != null && list.size == 1) {
                                initSchedule = list.get(0).target
                                targetTv.text = initSchedule.toString() + "%"
                            } else {
                                initSchedule = 0
                                targetTv.hint = "本月未设置"
                            }
                        } else {
                            Log.e("异常-----》", e.toString())
                            targetTv.hint = "本月未设置"
                        }
                    }
                })
    }

    fun getSchedule(project: Project) {
        val categoryBmobQuery = BmobQuery<ProjSchedule>()
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(object : FindListener<ProjSchedule>() {
            override fun done(list: MutableList<ProjSchedule>?, e: BmobException?) {
                if (e == null) {
                    scheduleList.clear()
                    scheduleList.addAll(list!!)
                    scheduleList.add(projSchedule)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 103) {
            if (data != null) {
                val schedule = data.getIntExtra("schedule", project.schedule)
                project.schedule = schedule
                scheduleTv.text = project.schedule.toString() + "%"
                checkBtnShow(schedule)

                getSchedule(project)
            }
        }
    }

    fun checkBtnShow(process: Int) {
        if (process == 100) {
            bt_comment.visibility = View.GONE
        }
    }
}
