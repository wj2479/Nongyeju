package com.qdhc.ny.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.baoyz.actionsheet.ActionSheet
import com.qdhc.ny.activity.AddReportActivity
import com.qdhc.ny.adapter.MyFragmentPagerAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.common.Constant
import kotlinx.android.synthetic.main.fragment_report.*


class ReportFragment : BaseFragment(), ActionSheet.ActionSheetListener {

    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var mAdapter: MyFragmentPagerAdapter

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ReportFragment {
            val fragment = ReportFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.fragment_report
    }

    override fun initView() {
        setupWithViewPager()
    }

    /**
     * Description：初始化FragmentPagerAdapter适配器并给ViewPager设置上该适配器，最后关联TabLayout和ViewPager
     */
    private fun setupWithViewPager() {
        var mTitles = context?.resources?.getStringArray(com.qdhc.ny.R.array.report_titles)!!
        mTitles.forEach { title ->
            mTabLayout.addTab(mTabLayout.newTab().setText(title))
        }

        val mFragments = ArrayList<Fragment>()
        mFragments.add(DayReportFragment(Constant.REPORT_TYPE_DAY))
        mFragments.add(DayReportFragment(Constant.REPORT_TYPE_WEEK))
        mFragments.add(DayReportFragment(Constant.REPORT_TYPE_MONTH))

        mAdapter = MyFragmentPagerAdapter(activity?.getSupportFragmentManager())
        mAdapter.addTitlesAndFragments(mTitles, mFragments)

        mViewPager.setOffscreenPageLimit(mFragments.size)
        mViewPager.setAdapter(mAdapter) // 给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager) //关联TabLayout和ViewPager
    }

    override fun initData() {
    }

    override fun lazyLoad() {
        getData()
    }

    override fun initClick() {
//        addIv.setOnClickListener {
//            ActionSheet.createBuilder(context, activity?.getSupportFragmentManager())
//                    .setCancelButtonTitle("取消")
//                    .setOtherButtonTitles("日报", "周报", "月报")
//                    .setCancelableOnTouchOutside(true)
//                    .setListener(this).show();
//        }
    }

    override fun onOtherButtonClick(actionSheet: ActionSheet?, index: Int) {
        var intent = Intent(context, AddReportActivity::class.java)
        intent.putExtra("type", index + 1)
        startActivity(intent)
    }

    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {
    }

    //获取数据
    fun getData() {

    }
}
