package com.qdhc.ny.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qdhc.ny.R;
import com.qdhc.ny.bmob.UserInfo;

import java.util.List;

/**
 * 通讯录
 */

public class ContactsAdapter extends BaseQuickAdapter<UserInfo, BaseViewHolder> {
    Activity mContext;

    String[] roles;

    public ContactsAdapter(Activity mContext, @Nullable List<UserInfo> data) {
        super(R.layout.item_select_manager, data);
        this.mContext = mContext;
        roles = mContext.getResources().getStringArray(R.array.roles);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo item) {
        //姓名
        helper.setText(R.id.tv_name, item.getNickName());
        //手机号
        helper.setText(R.id.tv_phone, "手机号:" + item.getMobilePhoneNumber());
        //用户名
        helper.setText(R.id.tv_username, "用户名:" + item.getUsername());

        helper.setText(R.id.tv_role_name, roles[item.getRole() - 1]);
    }

}
