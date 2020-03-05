package com.qdhc.ny.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qdhc.ny.R;
import com.qdhc.ny.bmob.Project;
import com.qdhc.ny.bmob.UserInfo;
import com.qdhc.ny.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * 信息列表
 */

public class ProjectAdapter extends BaseQuickAdapter<Project, BaseViewHolder> {
    Activity mContext;
    UserInfo userInfo = null;

    public ProjectAdapter(Activity mContext, @Nullable List<Project> data) {
        super(R.layout.item_project, data);
        this.mContext = mContext;
        userInfo = SharedPreferencesUtils.loadLogin(mContext);
    }

    @Override
    protected void convert(final BaseViewHolder helper, Project item) {
        helper.setText(R.id.tv_title, item.getName());

        helper.setText(R.id.tv_status, "进度: " + item.getSchedule() + "%");
        helper.setText(R.id.tv_introduce, item.getIntroduce());
//        helper.setText(R.id.tv_village, item.getVillage());
//        helper.setText(R.id.tv_district, item.getDistrict());

        helper.setText(R.id.tv_from, "上传时间: " + item.getUpdatedAt().substring(0, 10));

    }

}
