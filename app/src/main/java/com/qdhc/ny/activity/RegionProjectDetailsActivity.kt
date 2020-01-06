package com.qdhc.ny.activity

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.qdhc.ny.R
import com.qdhc.ny.adapter.RegionProjectAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.RegionProject
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration
import kotlinx.android.synthetic.main.activity_region_project_details.*

/**
 * 区域项目详情
 */
class RegionProjectDetailsActivity : BaseActivity() {

    lateinit var projectList: ArrayList<RegionProject>

    override fun intiLayout(): Int {
        return R.layout.activity_region_project_details
    }

    override fun initView() {

    }

    override fun initClick() {
        backIv.setOnClickListener { finish() }
    }

    override fun initData() {
        if (intent.hasExtra("regionProject")) {
            projectList = intent.getSerializableExtra("regionProject") as ArrayList<RegionProject>
        } else {
            projectList = ArrayList<RegionProject>()
        }

        Log.e("传递数据-----》", "数据" + projectList.size)

        initRefresh()
    }

    lateinit var mAdapter: RegionProjectAdapter
    private fun initRefresh() {
        smrw!!.layoutManager = LinearLayoutManager(this)
        smrw!!.addItemDecoration(DefaultItemDecoration(ContextCompat.getColor(this, R.color.backgroundColor)))

        // RecyclerView Item点击监听。
        smrw.setSwipeItemClickListener { itemView, position ->
            if (projectList.size == 0) {
                return@setSwipeItemClickListener
            }

            var project = projectList.get(position)
            var intent = Intent(this, RegionProjectListActivity::class.java)
            intent.putExtra("regionProject", project)
            startActivity(intent)
        }

        mAdapter = RegionProjectAdapter(this, projectList)
        smrw.adapter = mAdapter
    }
}
