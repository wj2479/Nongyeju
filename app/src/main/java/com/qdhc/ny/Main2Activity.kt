package com.qdhc.ny

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.google.gson.Gson
import com.luck.picture.lib.permissions.RxPermissions
import com.qdhc.ny.adapter.TabFragmentPagerAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.TabIconBean
import com.qdhc.ny.fragment.ContactsFragment
import com.qdhc.ny.fragment.ContradictionListFragment
import com.qdhc.ny.fragment.MessageFragment
import com.qdhc.ny.fragment.MyFragment
import com.sj.core.net.EventMsg
import com.sj.core.net.Rx.databus.RegisterRxBus
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class Main2Activity : BaseActivity() {

    var gson = Gson()
    override fun intiLayout(): Int {
        return (R.layout.activity_main)
    }

    override fun initClick() {
    }

    override fun initData() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        var rxPermission = RxPermissions(this)

        rxPermission
                .requestEach(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .subscribe({ permission ->
                    if (permission.granted) {
                        // 用户已经同意该权限
                        Log.d("", permission.name + " is granted.")
                        if (permission.name.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        }
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
                        ToastUtil.show(mContext, "拒绝了该权限，没有选中『不再询问』")

                        Log.d("", permission.name + " is denied. More info should be provided.")
                    } else {
                        // 用户拒绝了该权限，而且选中『不再询问』
                        ToastUtil.show(mContext, "拒绝了该权限，而且选中『不再询问』")

                        Log.d("", permission.name + " is denied.")
                    }

                })
    }


    //UI
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mIconUnselectIds = intArrayOf(R.drawable.ic_list,
            R.drawable.ic_schedule,
            R.drawable.ic_quality,
            R.drawable.ic_my)
    private val mIconSelectIds = intArrayOf(R.drawable.ic_list_select,
            R.drawable.ic_schedule_select,
            R.drawable.ic_quality_select,
            R.drawable.ic_my_select)

    override fun initView() {
        uploadIv.visibility = View.GONE
        //获取数据 在values/arrays.xml中进行定义然后调用
        var tabTitle = resources.getStringArray(R.array.tab2_titles)
        //将fragment装进列表中
        var fragmentList = ArrayList<Fragment>()
        fragmentList.add(ContradictionListFragment())
        fragmentList.add(MessageFragment())
        fragmentList.add(ContactsFragment())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
                tl.currentTab = 3
            }
        }
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

    @RegisterRxBus()
    public fun startLogin(eventMsg: EventMsg) {
        if (eventMsg != null) {
            startActivity(Intent(mContext, LoginActivity::class.java))
        }
    }

}
