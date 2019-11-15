package com.qdhc.ny.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.qdhc.ny.activity.ContradictionInfoActivity
import com.qdhc.ny.adapter.ContradictionAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bmob.Contradiction
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration
import kotlinx.android.synthetic.main.activity_sign_in_sear.*


class ContradictionListFragment : BaseFragment() {

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ContradictionListFragment {
            val fragment = ContradictionListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.fragment_contron_list
    }

    lateinit var userInfo: UserInfo

    override fun initView() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
    }

    override fun initClick() {
    }

    override fun initData() {
        initRefresh()
    }

    override fun lazyLoad() {
    }

    var datas = ArrayList<Contradiction>()
    lateinit var mAdapter: ContradictionAdapter
    private fun initRefresh() {
        smrw!!.layoutManager = LinearLayoutManager(activity)
        smrw!!.addItemDecoration(DefaultItemDecoration(ContextCompat.getColor(activity!!, com.qdhc.ny.R.color.backgroundColor)))

        // RecyclerView Item点击监听。
        smrw.setSwipeItemClickListener { itemView, position ->
            if (datas.size == 0) {
                return@setSwipeItemClickListener
            }

            var contradiction = datas.get(position)
            var intent = Intent(context, ContradictionInfoActivity::class.java)
            intent.putExtra("info", contradiction)
            startActivity(intent)
        }

        mAdapter = ContradictionAdapter(activity, datas)
        smrw.adapter = mAdapter
        val emptyView = layoutInflater.inflate(com.qdhc.ny.R.layout.common_empty, null)
        emptyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        emptyView.findViewById<TextView>(com.qdhc.ny.R.id.tv_empty).text = "暂无数据"
        //添加空视图
        mAdapter.emptyView = emptyView
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    //获取数据
    fun getData() {
        val categoryBmobQuery = BmobQuery<Contradiction>()
        if (userInfo.role == 1) {    // 1级用户 智能查看自己的数据
            categoryBmobQuery.addWhereEqualTo("uploader", userInfo.objectId)
        } else if (userInfo.role == 2) {  // 2级用户 只能查看本区县的信息
            categoryBmobQuery.addWhereEqualTo("district", userInfo.district)
        }
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(
                object : FindListener<Contradiction>() {
                    override fun done(list: List<Contradiction>?, e: BmobException?) {
                        if (e == null) {
                            Log.i("结果-----》", list?.size.toString())
                            datas.clear()
                            datas.addAll(list!!)
                            mAdapter.notifyDataSetChanged()
                        } else {
                            Log.i("异常-----》", e.toString())
                        }
                    }
                })
    }

}
