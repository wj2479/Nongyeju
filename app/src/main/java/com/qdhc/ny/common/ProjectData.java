package com.qdhc.ny.common;

import com.amap.api.location.AMapLocation;
import com.qdhc.ny.bmob.Notice;
import com.qdhc.ny.bmob.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wj
 * @Date 2019/11/13
 * @Desc
 * @Url http://www.chuangze.cn
 */
public class ProjectData {

    /**
     * 当前工程的列表
     */
    private List<Project> projects = new ArrayList<>();

    /**
     * 当前定位的位置
     */
    private AMapLocation location = null;

    /**
     * 公告列表
     */
    private List<Notice> notices = new ArrayList<>();

    private static ProjectData mPorjectData = null;

    private ProjectData() {
    }

    public static ProjectData getInstance() {
        if (mPorjectData == null) {
            synchronized (ProjectData.class) {
                if (mPorjectData == null)
                    mPorjectData = new ProjectData();
            }
        }
        return mPorjectData;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public AMapLocation getLocation() {
        return location;
    }

    public void setLocation(AMapLocation location) {
        this.location = location;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }
}
