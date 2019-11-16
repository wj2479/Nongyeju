package com.qdhc.ny.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.baoyz.actionsheet.ActionSheet
import com.qdhc.ny.activity.AddReportActivity
import com.qdhc.ny.adapter.MyFragmentPagerAdapter
import com.qdhc.ny.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contacts_tab.*

class ContactsTabFragment : BaseFragment(), ActionSheet.ActionSheetListener {

    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var mAdapter: MyFragmentPagerAdapter

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ContactsTabFragment {
            val fragment = ContactsTabFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.fragment_contacts_tab
    }

    override fun initView() {
        setupWithViewPager()
    }

    /**
     * Description：初始化FragmentPagerAdapter适配器并给ViewPager设置上该适配器，最后关联TabLayout和ViewPager
     */
    private fun setupWithViewPager() {
        var mTitles = arrayOf("东港区", "莒县", "五莲")
        mTitles.forEach { title ->
            mTabLayout.addTab(mTabLayout.newTab().setText(title))
        }

        val mFragments = ArrayList<Fragment>()
        mFragments.add(ContactsFragment(1, false))
        mFragments.add(ContactsFragment(2, false))
        mFragments.add(ContactsFragment(3, false))

        mAdapter = MyFragmentPagerAdapter(childFragmentManager)
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
