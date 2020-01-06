package com.qdhc.ny.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qdhc.ny.R;
import com.qdhc.ny.bean.RegionProject;

import java.util.List;

/**
 * 信息列表
 */
public class RegionProjectAdapter extends BaseQuickAdapter<RegionProject, BaseViewHolder> {
    Activity mContext;

    public RegionProjectAdapter(Activity mContext, @Nullable List<RegionProject> data) {
        super(R.layout.item_region_project, data);
        this.mContext = mContext;
    }

    @Override
    protected void convert(final BaseViewHolder helper, RegionProject item) {

        helper.setText(R.id.nameTv, item.getRegion().getName());
        helper.setText(R.id.targetTv, "- -");
        helper.setText(R.id.nowTv, item.getNowProcess() + "%");
    }

}
