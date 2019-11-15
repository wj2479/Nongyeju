package com.qdhc.ny.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.datatype.BmobDate
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.trace.LBSTraceClient
import com.amap.api.trace.TraceLocation
import com.amap.api.trace.TraceOverlay
import com.qdhc.ny.R
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bean.SignListInfo
import com.qdhc.ny.bmob.Tracks
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.dialog.RxDialogWheelYearMonthDay
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_trace_record.*
import kotlinx.android.synthetic.main.layout_title_theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


/**
 * 轨迹记录
 * @author shenjian
 * @date 2019/3/23
 */
class TraceRecordActivity : BaseActivity() {

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    lateinit var userInfo: UserInfo

    override fun intiLayout(): Int {
        return R.layout.activity_trace_record
    }

    lateinit var mAMap: AMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mAMap = mapView.map
        mAMap.uiSettings.isRotateGesturesEnabled = false
        mAMap.uiSettings.isZoomControlsEnabled = false
    }

    var date = ""
    val cal = Calendar.getInstance()

    override fun initView() {
        title_tv_title.text = "轨迹记录"
        title_tv_right.text = "选择人员"

        var date = "" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE)
        tv_date.text = date
        initWheelYearMonthDayDialog()
        initTrack()
    }

    lateinit var mRxDialogWheelYearMonthDay: RxDialogWheelYearMonthDay
    private fun initWheelYearMonthDayDialog() {
        // ------------------------------------------------------------------选择日期开始
        mRxDialogWheelYearMonthDay = RxDialogWheelYearMonthDay(this)
        mRxDialogWheelYearMonthDay.sureView.setOnClickListener {
            var date_txt = ("" +
                    mRxDialogWheelYearMonthDay.selectorYear + "-"
                    + mRxDialogWheelYearMonthDay.selectorMonth + "-"
                    + mRxDialogWheelYearMonthDay.selectorDay)
            tv_date.text = date_txt

            getPoints(userInfo.objectId, date_txt)
            mRxDialogWheelYearMonthDay.cancel()
        }
        mRxDialogWheelYearMonthDay.cancleView.setOnClickListener(
                { mRxDialogWheelYearMonthDay.cancel() })
        // ------------------------------------------------------------------选择日期结束
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }
        title_tv_right.setOnClickListener { startActivityForResult(Intent(mContext, MyClientManageActivity::class.java), 1) }
        tv_date.setOnClickListener { mRxDialogWheelYearMonthDay.show() }

        iv_bottom_road.setOnClickListener {
            //路线
        }
        iv_bottom_location.setOnClickListener {
            //定位
        }
        iv_bottom_count.setOnClickListener {
            //            //统计
//            startActivity(Intent(mContext, TraceAnalysisActivity::class.java)
//                    .putExtra("userId", mUserId)
//                    .putExtra("title", title_tv_title.text.toString())
//                    .putExtra("date", tv_date.text.toString()))
        }
    }

    override fun initData() {
        userInfo = SharedPreferencesUtils.loadLogin(this)
        var date = "" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE)
        title_tv_title.text = userInfo.nickName + "的运动轨迹"

//        Log.e("TAG", "日期--> " + date)
        getPoints(userInfo.objectId, date)
    }

    /**
     * 根据用户ID  日期获取 轨迹点记录
     */
    fun getPoints(objectId: String, date: String) {
        var createdAtStart = date + " 00:00:00";
        var createdAtDateStart = sdf.parse(createdAtStart);
        var bmobCreatedAtDateStart = BmobDate(createdAtDateStart);

        var createdAtEnd = date + " 23:59:59";
        var createdAtDateEnd = sdf.parse(createdAtEnd);
        var bmobCreatedAtDateEnd = BmobDate(createdAtDateEnd);

        var bmobQuery = BmobQuery<Tracks>();
        bmobQuery.addWhereGreaterThanOrEqualTo("createdAt", bmobCreatedAtDateStart);
        bmobQuery.addWhereLessThanOrEqualTo("createdAt", bmobCreatedAtDateEnd);
        bmobQuery.addWhereEqualTo("uid", objectId)
        bmobQuery.order("locationTime")
//        bmobQuery.order("createdAt")

        bmobQuery.findObjects(object : FindListener<Tracks>() {
            override fun done(list: MutableList<Tracks>?, e: BmobException?) {
                if (e == null) {
                    Log.e("TAG", "轨迹--> " + list.toString())
                    if (list?.size == 0) {
                        ToastUtil.show(this@TraceRecordActivity, "您在当天没有运动轨迹")
                        clearTracksOnMap()
                    } else {
                        drawTrackOnMap(list!!)
                        getSignLocationList(userInfo.objectId, date)
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data.let {
            if (resultCode == Activity.RESULT_OK && requestCode == 1) {
//                title_tv_title.text = it!!.getParcelableExtra<UserInfo>("user").nickName
//                getPoint(it!!.getParcelableExtra<UserInfo>("user").userid, tv_date.text.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState)
    }

    private var polylines = LinkedList<Polyline>()
    private var endMarkers = LinkedList<Marker>()
    val boundsBuilder = LatLngBounds.Builder()

    private fun drawTrackOnMap(points: List<Tracks>) {
        clearTracksOnMap()
        val polylineOptions = PolylineOptions()
        //  original.clear()
        //  original.addAll(points)
        polylineOptions.width(20f)
        polylineOptions.customTexture = BitmapDescriptorFactory
                .fromAsset("icon_road_green_arrow.png")
        polylineOptions.isUseTexture = true
        //       自己划线前清空一下
        Log.e(" points.size:", " points.size:" + points.size)

        points.forEachIndexed { index, it ->
            var traceLocation = TraceLocation(it.lat, it.lng, it.speed, it.direction, it.locationTime + 1)
            mTraceList.add(traceLocation)

            //自己划线
            val latLng = LatLng(it.lat, it.lng)

            polylineOptions.add(latLng)
            boundsBuilder.include(latLng)
            val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("时间：" + points[index].createdAt)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(resources, R.drawable.ic_analysis_worker_ing)))
            endMarkers.add(mAMap.addMarker(markerOptions))
        }
        val polyline = mAMap.addPolyline(polylineOptions)
        polylines.add(polyline)
        //移动到可视范围
        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 24))
        if (points.isNotEmpty()) {
            // 起点
            val p = points[0]
            val latLng = LatLng(p.lat, p.lng)
            val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("时间：" + points[0].createdAt)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(resources, R.drawable.ic_analysis_worker_start)))
            endMarkers.add(mAMap.addMarker(markerOptions))
        }
        if (points.size > 1) {
            // 终点
            val p = points[points.size - 1]
            val latLng = LatLng(p.lat, p.lng)
            val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("时间：" + points[points.size - 1].createdAt)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(resources, R.drawable.ic_analysis_worker_stop)))
            endMarkers.add(mAMap.addMarker(markerOptions))
        }
    }

    /**
     * 清除地图已完成或出错的轨迹
     */
    private fun clearTracksOnMap() {
        for (polyline in polylines) {
            polyline.remove()
        }
        for (marker in endMarkers) {
            marker.remove()
        }
        endMarkers.clear()
        mOverlayList.forEach { (key, overlay) ->
            if (overlay.traceStatus == TraceOverlay.TRACE_STATUS_FINISH
                    || overlay.traceStatus == TraceOverlay.TRACE_STATUS_FAILURE) {
                overlay.remove()
                mOverlayList.remove(key)
            }
        }
    }

    var mTraceList = ArrayList<TraceLocation>()
    val mOverlayList = ConcurrentHashMap<Int, TraceOverlay>()

    lateinit var mTraceClient: LBSTraceClient
    private fun initTrack() {
        mTraceClient = LBSTraceClient.getInstance(this.applicationContext)
    }

    /***
     *获取当天签到数据
     * [userid] 查询的用户id
     * [date] 查询当天时间
     */
    fun getSignLocationList(userId: String, date: String) {
    }

    private fun initSignLocation(locationInfos: ArrayList<SignListInfo.DataListBean>) {
        if (locationInfos.size > 0) {
//            if (endMarkers.size<=1){
//                var cameraUpdate = CameraUpdateFactory
//                        .newCameraPosition(CameraPosition(LatLng(locationInfos[0].lat, locationInfos[0].lon), 12f, 0f, 0f))
//                mAMap.moveCamera(cameraUpdate)
//            }

            locationInfos.forEach {
                val latLng = LatLng(it.lat, it.lon)
                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("时间：" + it.addDate +
                                "\n签到地点：" + it.addressAuto +
                                "\n签到类型：" + it.category +
                                "\n拜访对象：" + it.personName)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(resources, R.drawable.ic_analysis_worker_sgin)))
                endMarkers.add(mAMap.addMarker(markerOptions))
            }
        }
    }
}
