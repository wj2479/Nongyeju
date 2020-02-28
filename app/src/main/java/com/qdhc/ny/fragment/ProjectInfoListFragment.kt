package com.qdhc.ny.fragment


import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.Log
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.qdhc.ny.R
import com.qdhc.ny.activity.RegionProjectDetailsActivity
import com.qdhc.ny.adapter.ProjectPageAdapter
import com.qdhc.ny.base.BaseFragment
import com.qdhc.ny.bean.RegionProject
import com.qdhc.ny.bmob.Area_Region
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.eventbus.RegionEvent
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.qdhc.ny.view.MyAxisValueFormatter
import com.qdhc.ny.view.MyValueFormatter
import com.qdhc.ny.view.XAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_project_into_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList


/**
 * 工程信息列表
 */
class ProjectInfoListFragment : BaseFragment() {

    var projectList = ArrayList<RegionProject>()
    lateinit var mAdapter: ProjectPageAdapter
    lateinit var userInfo: UserInfo

//    lateinit var mChart: BarChart

    override fun intiLayout(): Int {
        return R.layout.fragment_project_into_list
    }

    override fun initView() {
        mChart.setNoDataText("")
//        initBarChart()
//        setBarChartData(entryList)
    }

    override fun initClick() {
        detailsIv.setOnClickListener {
            var intent = Intent(context, RegionProjectDetailsActivity::class.java)
            intent.putExtra("regionProject", projectList)
            startActivity(intent)
        }
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(context)
//        getProjectData()
    }

    override fun lazyLoad() {

    }

    /**
     * 根据地区获取工程
     */
    fun getProjectByRegion(index: Int, areaRegion: Area_Region) {
        val categoryBmobQuery = BmobQuery<Project>()

        when (areaRegion.region_level) {
            2 -> {   // 市级
            }
            3 -> {   // 区县级
                categoryBmobQuery.addWhereEqualTo("county", areaRegion.objectId)
            }
            4 -> {    // 乡镇级
                categoryBmobQuery.addWhereEqualTo("village", areaRegion.objectId)
            }
        }
        categoryBmobQuery.order("-createdAt")
        categoryBmobQuery.findObjects(object : FindListener<Project>() {
            override fun done(list: List<Project>?, e: BmobException?) {
                if (e == null) {
                    if (list != null) {
                        var size = list.size
                        var sum = 0
                        list.forEach { project ->
                            sum += project.schedule
                        }
                        var process = (sum.toFloat() / size).toInt()
                        Log.e("地区工程-----》", areaRegion.name + "-->" + process.toString())
                        entryList.add(BarEntry(index.toFloat(), process.toFloat()))

                        projectList.get(index).projectList = list
                        projectList.get(index).nowProcess = process

                        count++
                        if (count == MAX_COUNT) {
                            if (ProjectData.getInstance().myRegion != null) {
                                tv_title.text = ProjectData.getInstance().myRegion.name + "高标准农田建设"
                            }

                            initBarChart(ProjectData.getInstance().subRegion)
                            Collections.sort(entryList, object : Comparator<BarEntry> {
                                override fun compare(o1: BarEntry?, o2: BarEntry?): Int {
                                    if (o1 != null && o2 != null) {
                                        return o1.x.toInt() - o2.x.toInt()
                                    } else
                                        return 0
                                }
                            })
                            setBarChartData(entryList)
                            initTotalProcess()

                            mChart.notifyDataSetChanged(); // let the chart know it's data changed
                            mChart.invalidate(); // refresh
                            Log.e("地区工程-----》", "设置数据")
                        }
                    }
                } else {
                    Log.e("工程异常-----》", e.toString())
                }
            }
        })
    }

    /**
     * 初始化总进度
     */
    private fun initTotalProcess() {
        var size = entryList.size
        var sum = 0
        entryList.forEach { project ->
            sum += project.y.toInt()
        }
        var process = (sum.toFloat() / size).toInt()
        total_procss_tv.text = process.toString() + "%"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RegionEvent) {
        Log.e("下级结果onMessageEvent", ProjectData.getInstance().subRegion.toString())

        initBarChart()
    }

    internal var MAX_COUNT = 0
    internal var count = 0

    var entryList = ArrayList<BarEntry>()

    private fun initBarChart() {
        MAX_COUNT = ProjectData.getInstance().subRegion.size
        count = 0

        ProjectData.getInstance().subRegion.forEachIndexed { index, areaRegion ->
            var regionProject = RegionProject()
            regionProject.region = areaRegion
            projectList.add(regionProject)
            getProjectByRegion(index, areaRegion)
        }
    }

    /**
     * 初始化柱形图控件属性
     */
    private fun initBarChart(regionList: List<Area_Region>) {
//        mChart.setOnChartValueSelectedListener(this)
        mChart.setDrawBarShadow(false)
        mChart.setDrawValueAboveBar(true)
        mChart.getDescription().setEnabled(false)
        mChart.setTouchEnabled(false); // 设置是否可以触摸
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60)
        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false)
        mChart.setDrawGridBackground(false)

        //自定义坐标轴适配器，配置在X轴，xAxis.setValueFormatter(xAxisFormatter);
        val xAxisFormatter = XAxisValueFormatter(regionList)

        val xAxis = mChart.getXAxis()
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
//        xAxis.setTypeface(mTfLight)//可以去掉，没什么用
        xAxis.setDrawAxisLine(false)
        xAxis.setGranularity(1f)
        xAxis.setValueFormatter(xAxisFormatter)

        //自定义坐标轴适配器，配置在Y轴。leftAxis.setValueFormatter(custom);
        val custom = MyAxisValueFormatter()

        //获取到图形左边的Y轴
        val leftAxis = mChart.getAxisLeft()
//        leftAxis.addLimitLine(limitLine)
//        leftAxis.setTypeface(mTfLight)//可以去掉，没什么用
        leftAxis.setLabelCount(5, false)
        leftAxis.setValueFormatter(custom)
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.setSpaceTop(10f)
        leftAxis.setAxisMinimum(0f)
        leftAxis.setAxisMaxValue(100f)

        //获取到图形右边的Y轴，并设置为不显示
        mChart.getAxisRight().setEnabled(false)

        //图例设置
        val legend = mChart.getLegend()
        //不显示图例
        legend.setForm(Legend.LegendForm.NONE);

        //如果点击柱形图，会弹出pop提示框.XYMarkerView为自定义弹出框
//        val mv = XYMarkerView(this, xAxisFormatter)
//        mv.setChartView(mChart)
//        mChart.setMarker(mv)
//        setBarChartData()
    }

    private fun setBarChartData(entryList: List<BarEntry>) {
        var set1: BarDataSet;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = mChart.getData().getDataSetByIndex(0) as BarDataSet;
            set1.setValues(entryList);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = BarDataSet(entryList, "");
            //是否显示顶部的值
            set1.setDrawValues(true);
            // 设置顶部的格式方式
            set1.setValueFormatter(MyValueFormatter())

            val startColor1 = ContextCompat.getColor(context!!, R.color.text_color_green)
            val startColor2 = ContextCompat.getColor(context!!, R.color.text_color_orange)
            set1.setColors(startColor1, startColor2)

            var dataSets = ArrayList<IBarDataSet>();
            dataSets.add(set1);

            var data = BarData(dataSets);
            data.setValueTextSize(10f);
//                data.setValueTypeface(mTfLight);//可以去掉，没什么用
            data.setBarWidth(0.3f);
            mChart.setData(data);
        }
    }

}
