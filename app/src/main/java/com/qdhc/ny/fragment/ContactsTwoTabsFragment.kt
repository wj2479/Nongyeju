package com.qdhc.ny.fragment

import android.content.Intent
import android.support.v4.app.Fragment
import com.baoyz.actionsheet.ActionSheet
import com.qdhc.ny.R
import com.qdhc.ny.activity.AddReportActivity
import com.qdhc.ny.adapter.MyFragmentPagerAdapter
import com.qdhc.ny.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contacts_tab.*

class ContactsTwoTabsFragment : BaseFragment(), ActionSheet.ActionSheetListener {

    lateinit var mAdapter: MyFragmentPagerAdapter

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
        val mFragments = ArrayList<Fragment>()
        var mTitles = context?.resources?.getStringArray(R.array.areas)
        mTitles?.forEachIndexed { index, title ->
            mTabLayout.addTab(mTabLayout.newTab().setText(title))
            mFragments.add(ContactsTabFragment(index + 1, false))
        }

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
