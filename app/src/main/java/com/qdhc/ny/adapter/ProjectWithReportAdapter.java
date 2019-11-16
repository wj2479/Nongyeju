package com.qdhc.ny.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qdhc.ny.R;
import com.qdhc.ny.activity.ReportListActivity;
import com.qdhc.ny.bmob.Project;
import com.qdhc.ny.bmob.UserInfo;
import com.qdhc.ny.common.Constant;
import com.qdhc.ny.utils.UserInfoUtils;

import java.util.List;

/**
 * 信息列表
 */
public class ProjectWithReportAdapter extends BaseQuickAdapter<Project, BaseViewHolder> {
    Activity mContext;

    public ProjectWithReportAdapter(Activity mContext, @Nullable List<Project> data) {
        super(R.layout.item_project_report, data);
        this.mContext = mContext;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final Project item) {
        helper.setText(R.id.tv_title, item.getName());

        if (item.getSchedules() != null && item.getSchedules().size() > 0) {
            int schedule = item.getSchedules().get(0).getSchedule();
            helper.setText(R.id.tv_status, "进度: " + schedule + "%");
        } else {
            helper.setText(R.id.tv_status, "进度: 0%");
        }
        helper.setText(R.id.tv_introduce, item.getIntroduce());
//        helper.setText(R.id.tv_village, item.getVillage());
//        helper.setText(R.id.tv_district, item.getDistrict());

        UserInfoUtils.getInfoByObjectId(item.getManager(), new UserInfoUtils.IResult() {
            @Override
            public void onReslt(UserInfo userInfo) {
                if (userInfo != null)
                    helper.setText(R.id.tv_person, "负责人:" + userInfo.getNickName());
            }
        });

        helper.setText(R.id.tv_from, "创建时间: " + item.getCreatedAt().substring(0, 10));

        helper.setOnClickListener(R.id.tv_dayReport, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportListActivity.class);
                intent.putExtra("type", Constant.REPORT_TYPE_DAY);
                intent.putExtra("project", item);
                mContext.startActivity(intent);
            }
        });
        helper.setOnClickListener(R.id.tv_weekReport, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportListActivity.class);
                intent.putExtra("type", Constant.REPORT_TYPE_WEEK);
                intent.putExtra("project", item);
                mContext.startActivity(intent);
            }
        });

        helper.setOnClickListener(R.id.tv_monthReport, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportListActivity.class);
                intent.putExtra("type", Constant.REPORT_TYPE_MONTH);
                intent.putExtra("project", item);
                mContext.startActivity(intent);
            }
        });

    }

}
