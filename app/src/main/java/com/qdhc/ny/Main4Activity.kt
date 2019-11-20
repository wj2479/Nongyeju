package com.qdhc.ny

import android.support.v4.app.Fragment
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.google.gson.Gson
import com.qdhc.ny.adapter.TabFragmentPagerAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.TabIconBean
import com.qdhc.ny.fragment.ContactsFragment
import com.qdhc.ny.fragment.ContradictionListFragment
import com.qdhc.ny.fragment.MyFragment
import com.qdhc.ny.fragment.ReportListFragment
import com.qdhc.ny.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * 乡镇领导
 */
class Main4Activity : BaseActivity() {

    var gson = Gson()
    override fun intiLayout(): Int {
        return (R.layout.activity_main2)
    }

    override fun initClick() {
    }

    override fun initData() {
    }

    //UI
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mIconUnselectIds = intArrayOf(R.drawable.ic_schedule,
            R.drawable.ic_quality,
            R.drawable.ic_contacts,
            R.drawable.ic_my)
    private val mIconSelectIds = intArrayOf(R.drawable.ic_schedule_select,
            R.drawable.ic_quality_select,
            R.drawable.ic_contacts_select,
            R.drawable.ic_my_select)

    override fun initView() {

        var userInfo = SharedPreferencesUtils.loadLogin(this)

        //获取数据 在values/arrays.xml中进行定义然后调用
        var tabTitle = resources.getStringArray(R.array.tab2_titles)
        //将fragment装进列表中
        var fragmentList = ArrayList<Fragment>()
        fragmentList.add(ContradictionListFragment(userInfo.areaId, userInfo.district, true))
        fragmentList.add(ReportListFragment(userInfo.areaId, userInfo.district, true))
        fragmentList.add(ContactsFragment(userInfo.areaId, userInfo.district, true))
        fragmentList.add(MyFragment())
        //viewpager加载adapter
        vp.adapter = TabFragmentPagerAdapter(supportFragmentManager, fragmentList, tabTitle)
        for (i in fragmentList.indices) {
            mTabEntities.add(TabIconBean(tabTitle[i], mIconSelectIds[i], mIconUnselectIds[i]))
        }
        tl.setTabData(mTabEntities)
        initPager()
    }

    var tab_position = 0
    override fun onRestart() {
        super.onRestart()
        tl.currentTab = tab_position
    }

    private fun initPager() {
        tl.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                vp.currentItem = position
                tab_position = position
            }

            override fun onTabReselect(position: Int) {
                if (position == 0) {
                    //  tl.showMsg(0, 56)
                }
            }
        })

        //默认选中第一个
        vp.currentItem = 0
        vp.offscreenPageLimit = 5
        vp.setNoScroll(true)
    }

}
