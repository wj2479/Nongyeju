package com.qdhc.ny.fragment


import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.qdhc.ny.R
import com.qdhc.ny.activity.DailyReportDetailsActivity
import com.qdhc.ny.adapter.DailyReportAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.DailyReport
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration
import kotlinx.android.synthetic.main.activity_sign_in_sear.*


/**
 * 日报
 * A simple [Fragment] subclass.
 */
@SuppressLint("ValidFragment")
class DayReportFragment(project: Project) : BaseFragment() {

    lateinit var userInfo: UserInfo

    /**
     * 项目的日报记录
     */
    var reports = ArrayList<DailyReport>()

    lateinit var mAdapter: DailyReportAdapter

    var project: Project

    init {
        this.project = project
    }

    override fun intiLayout(): Int {
        return R.layout.fragment_day_report
    }

    override fun initView() {
        initRefresh()
    }

    private fun initRefresh() {
        smrw!!.layoutManager = LinearLayoutManager(context)
        smrw.addItemDecoration(DefaultItemDecoration(ContextCompat.getColor(context!!, R.color.backgroundColor)))
        // RecyclerView Item点击监听。
        smrw.setSwipeItemClickListener { itemView, position ->
            if (reports.size > position) {
                var report = reports[position]
                var intent = Intent(context, DailyReportDetailsActivity::class.java)
                intent.putExtra("report", report)
                startActivity(intent)
            }
        }

        mAdapter = DailyReportAdapter(activity, reports)
        smrw.adapter = mAdapter

        val emptyView = layoutInflater.inflate(com.qdhc.ny.R.layout.common_empty, null)
        emptyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        emptyView.findViewById<TextView>(R.id.tv_empty).text = "暂无记录"
        //添加空视图
        mAdapter.emptyView = emptyView
    }

    override fun initClick() {
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
    }

    override fun lazyLoad() {
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    //获取数据
    fun getData() {
        val categoryBmobQuery = BmobQuery<DailyReport>()
//        categoryBmobQuery.addWhereEqualTo("uid", userInfo.objectId)
        categoryBmobQuery.addWhereEqualTo("pid", project.objectId)
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(
                object : FindListener<DailyReport>() {
                    override fun done(list: List<DailyReport>?, e: BmobException?) {
                        if (e == null) {
                            reports.clear()
                            reports.addAll(list!!)
                            mAdapter.notifyDataSetChanged()
                        } else {
                            Log.e("异常-----》", e.toString())
                        }
                    }
                })
    }
}
