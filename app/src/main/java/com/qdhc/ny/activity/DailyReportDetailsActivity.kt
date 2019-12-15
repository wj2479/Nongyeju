package com.qdhc.ny.activity

import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.luck.picture.lib.PictureSelector
import com.qdhc.ny.R
import com.qdhc.ny.adapter.ImageAdapter
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.ContradictPic
import com.qdhc.ny.bmob.DailyReport
import com.qdhc.ny.utils.SharedPreferencesUtils
import com.qdhc.ny.utils.UserInfoUtils
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_daily_report_details.*
import kotlinx.android.synthetic.main.layout_title_theme.*


/**
 * 日志详情
 */
class DailyReportDetailsActivity : BaseActivity() {

    lateinit var adapter: ImageAdapter

    var selectList = ArrayList<ContradictPic>()

    override fun intiLayout(): Int {
        return R.layout.activity_daily_report_details
    }

    override fun initView() {
        title_tv_title.text = "日报详情"

        rlv.isNestedScrollingEnabled = false
        rlv.layoutManager = GridLayoutManager(this, 4) as RecyclerView.LayoutManager?
        adapter = ImageAdapter(this, selectList)
        adapter.setOnItemClickListener { position, v ->
            var url = selectList.get(position).file.url

            if (url.endsWith("mp4", true)) {
                PictureSelector.create(this@DailyReportDetailsActivity).externalPictureVideo(url);
            } else {
                var intent = Intent(this, ImageActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        }
        rlv.adapter = adapter
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }
    }

    override fun initData() {

        var report = intent.getSerializableExtra("report") as DailyReport

        nameTv.text = report.title
        locationTv.text = report.address
        timeTv.text = report.createdAt
        contentTv.text = report.content

        when (report.check) {
            0 -> checkTv.text = "合格"
            1 -> checkTv.text = "一般"
            2 -> checkTv.text = "不合格"
        }

        var userInfo = SharedPreferencesUtils.loadLogin(this)

        if (userInfo.objectId.equals(report.uid)) {
            personLayout.visibility = View.GONE
            locationLayout.visibility = View.GONE
        } else {
            UserInfoUtils.getInfoByObjectId(report.uid, { userInfo ->
                if (userInfo != null) {
                    personTv.text = userInfo.nickName
                } else {
                    personLayout.visibility = View.GONE
                }
            })
        }

        getImags(report)
    }

    fun getImags(report: DailyReport) {
        if (report == null || report.objectId == null) {
            photoLayout.visibility = View.GONE
            return
        }

        var query = BmobQuery<ContradictPic>()
        query.addWhereEqualTo("contradict", report.objectId)
        query.findObjects(object : FindListener<ContradictPic>() {
            override fun done(`list`: List<ContradictPic>, e: BmobException?) {
                if (e == null) {
                    Log.e("TAG", "获取图片->" + list.size.toString())
                    if (list.size > 0) {
                        list.forEach { result ->
                            if (result.file.url.endsWith("mp4", true)) {
//                                result.file.download()
//                                var localMedia = LocalMedia()
//                                localMedia.path = url
//                                localMedia.mimeType = PictureMimeType.ofVideo()
//                                // 获取时长
//                                var mediaPlayer = MediaPlayer();
//                                mediaPlayer.setDataSource(url);
//                                mediaPlayer.prepare();
//                                localMedia.duration = mediaPlayer.getDuration().toLong() + 1000
//                                localMedia.pictureType = "video/mp4"
//                                localMedia.cutPath = path
//                                localMedia.isCut = true
//                                selectList.add(localMedia)

                            } else {
//                                var localMedia = LocalMedia()
//                                localMedia.path = result.file.url
//                                localMedia.mimeType = PictureMimeType.ofImage()
//                                localMedia.pictureType = "image/jpeg"
//                                selectList.add(localMedia)
                            }
                        }

//                        adapter = GridImageAdapter(this@ReportDetailsActivity, selectList.size, null)
//                        adapter.setOnItemClickListener { position, v ->
//                            var localMedia = selectList.get(position)
//                            if (localMedia.mimeType == PictureMimeType.ofImage()) {
//                                var intent = Intent(this@ReportDetailsActivity, ImageActivity::class.java)
//                                intent.putExtra("url", localMedia.path)
//                                startActivity(intent)
//                            } else if (localMedia.mimeType == PictureMimeType.ofVideo()) {
//                                PictureSelector.create(this@ReportDetailsActivity).externalPictureVideo(localMedia.path);
//                            }
//                        }
//                        adapter.setList(selectList)
//                        rlv.adapter = adapter

                        selectList.clear()
                        selectList.addAll(list)
                        adapter.notifyDataSetChanged()

                    } else {
                        photoLayout.visibility = View.GONE
                    }
                } else {
                    Log.e("TAG", "获取照片失败:->" + e.toString())
                    ToastUtil.show(this@DailyReportDetailsActivity, "获取照片失败")
                    photoLayout.visibility = View.GONE
                }
            }
        })
    }

}
