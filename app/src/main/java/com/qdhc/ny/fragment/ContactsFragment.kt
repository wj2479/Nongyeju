package com.qdhc.ny.fragment

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qdhc.ny.R
import com.qdhc.ny.activity.UserAddActivity
import com.qdhc.ny.adapter.ContactsAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.UserInfo
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration
import kotlinx.android.synthetic.main.fragment_contacts.*

class ContactsFragment() : BaseFragment() {

    var isShowTitle = true

    init {
    }

    override fun intiLayout(): Int {
        return R.layout.fragment_contacts
    }

    override fun initView() {
        if (!isShowTitle)
            titleLayout.visibility = View.GONE
        initRefresh()
    }

    override fun initData() {

    }

    override fun lazyLoad() {

    }

    override fun initClick() {
        addIv.setOnClickListener { v ->
            var intent = Intent(context, UserAddActivity::class.java)
            context?.startActivity(intent)
        }
    }

    var datas = ArrayList<UserInfo>()
    lateinit var mAdapter: ContactsAdapter

    private fun initRefresh() {
        smrw!!.layoutManager = LinearLayoutManager(activity)
        smrw!!.addItemDecoration(DefaultItemDecoration(ContextCompat.getColor(activity!!, R.color.line_wag)))

        mAdapter = ContactsAdapter(activity, datas)
        smrw.adapter = mAdapter

        val emptyView = layoutInflater.inflate(com.qdhc.ny.R.layout.common_empty, null)
        emptyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        emptyView.findViewById<TextView>(R.id.tv_empty).text = "暂无监理人员"
        //添加空视图
        mAdapter.emptyView = emptyView

    }

}
