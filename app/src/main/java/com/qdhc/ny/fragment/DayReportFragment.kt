package com.qdhc.ny.fragment


import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ExpandableListView
import com.qdhc.ny.activity.AddReportActivity
import com.qdhc.ny.activity.ReportDetailsActivity
import com.qdhc.ny.activity.ReportListActivity
import com.qdhc.ny.adapter.ReportExpandAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.Report
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.common.Constant
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.eventbus.ProjectEvent
import com.qdhc.ny.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.fragment_day_report.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 日报
 * A simple [Fragment] subclass.
 */
@SuppressLint("ValidFragment")
class DayReportFragment(type: Int) : BaseFragment() {

    lateinit var userInfo: UserInfo

    var reportProjects = ArrayList<Project>()

    lateinit var mAdapter: ReportExpandAdapter

    var type = 0

    init {
        this.type = type
    }

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.fragment_day_report
    }

    override fun initView() {
        mAdapter = ReportExpandAdapter(context, reportProjects, type)
        expandLv.setAdapter(mAdapter)
        expandLv.setGroupIndicator(null);

        expandLv.setOnChildClickListener(object : ExpandableListView.OnChildClickListener {
            override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
                var report: Report? = null;
                var project = reportProjects.get(groupPosition);

                when (type) {
                    Constant.REPORT_TYPE_DAY -> report = project.dayRreports.get(childPosition)
                    Constant.REPORT_TYPE_WEEK -> report = project.weekRreports.get(childPosition)
                    Constant.REPORT_TYPE_MONTH -> report = project.monthRreports.get(childPosition)
                }
//                Log.e("TAG", "点击事件--->" + report)

                var intent = Intent(context, ReportDetailsActivity::class.java)
                intent.putExtra("project", project)
                intent.putExtra("report", report)
                startActivity(intent)
                return true
            }
        })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ProjectEvent) {
//        Log.e("TAG", "进度事件--->" + ProjectData.getInstance().projects)
        reportProjects.clear()
        reportProjects.addAll(ProjectData.getInstance().projects)
        mAdapter.notifyDataSetChanged()
        expandGroup()
    }

    fun expandGroup() {
        val groupCount = mAdapter.groupCount
        for (i in 0 until groupCount) {
            expandLv.expandGroup(i)
        }
    }

    override fun initClick() {
        mAdapter.setOnViewButtonClickListener(object : ReportExpandAdapter.OnViewButtonClickListener {
            override fun onAddButtonClick(groupPosition: Int) {
//                Log.e("TAG", "点击了添加按钮:" + groupPosition)
                var selectProject = reportProjects.get(groupPosition)
                var intent = Intent(context, AddReportActivity::class.java)
                intent.putExtra("project", selectProject)
                intent.putExtra("type", type)
                startActivity(intent)
            }

            override fun onMoreButtonClick(groupPosition: Int) {
                var intent = Intent(context, ReportListActivity::class.java)
                var selectProject = reportProjects.get(groupPosition)
                intent.putExtra("project", selectProject)
                intent.putExtra("type", type)
                startActivity(intent)
            }
        })
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
    }

    override fun lazyLoad() {
    }

}
