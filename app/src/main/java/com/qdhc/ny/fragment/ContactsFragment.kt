package com.qdhc.ny.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.widget.TextView
import com.qdhc.ny.R
import com.qdhc.ny.adapter.SelectManageAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.UserInfo
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration
import kotlinx.android.synthetic.main.fragment_contacts.*

class ContactsFragment : BaseFragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ContactsFragment {
            val fragment = ContactsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun intiLayout(): Int {
        return R.layout.fragment_contacts
    }

    override fun initView() {
        initRefresh()
    }


    override fun initData() {
    }

    override fun lazyLoad() {
        getData()
    }

    override fun initClick() {
    }

    var page = 1
    var datas = ArrayList<UserInfo>()
    lateinit var mAdapter: SelectManageAdapter

    private fun initRefresh() {
        main_srl.setOnRefreshListener {
            //刷新服务
            page = 1
            getData()
        }
        smrw!!.layoutManager = LinearLayoutManager(activity)
        smrw!!.addItemDecoration(DefaultItemDecoration(ContextCompat.getColor(activity!!, R.color.backgroundColor)))

        // RecyclerView Item点击监听。
        smrw.setSwipeItemClickListener { itemView, position ->
            //            if (datas[position].phone.length>=7){
//                var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + datas[position].phone))
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//            }

        }

        // 使用默认的加载更多的View
        smrw.useDefaultLoadMore()
        // 加载更多的监听
        smrw.setLoadMoreListener {
            page++
            getData()

        }
        mAdapter = SelectManageAdapter(activity, datas)
        smrw.adapter = mAdapter

        val emptyView = layoutInflater.inflate(com.qdhc.ny.R.layout.common_empty, null)
        emptyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        emptyView.findViewById<TextView>(R.id.tv_empty).text = "暂无联系人"
        //添加空视图
        mAdapter.emptyView = emptyView

    }

    //获取数据
    fun getData() {

    }
}
