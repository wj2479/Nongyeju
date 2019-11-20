package com.qdhc.ny.bmob;

import java.io.Serializable;
import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * 标段/项目
 *
 * @Author wj
 * @Date 2019/11/7
 * @Desc
 * @Url http://www.chuangze.cn
 */
public class Project extends BmobObject implements Serializable {

    /**
     * 项目名
     */
    String name;
    /**
     * 项目介绍
     */
    String introduce;

    /**
     * 监理负责人ID
     */
    private String manager;

    /**
     * 项目标签
     */
    String tags;

    /**
     * 所属区域
     */
    int area;
    /**
     * 所属村落ID
     */
    String village;

    /**
     * 项目的进度记录
     */
    List<ProjSchedule> schedules = null;

    /**
     * 项目的日报记录
     */
    List<Report> dayRreports = null;

    /**
     * 项目的周报记录
     */
    List<Report> weekRreports = null;

    /**
     * 项目的月报记录
     */
    List<Report> monthRreports = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public List<ProjSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ProjSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<Report> getDayRreports() {
        return dayRreports;
    }

    public void setDayRreports(List<Report> dayRreports) {
        this.dayRreports = dayRreports;
    }

    public List<Report> getWeekRreports() {
        return weekRreports;
    }

    public void setWeekRreports(List<Report> weekRreports) {
        this.weekRreports = weekRreports;
    }

    public List<Report> getMonthRreports() {
        return monthRreports;
    }

    public void setMonthRreports(List<Report> monthRreports) {
        this.monthRreports = monthRreports;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", introduce='" + introduce + '\'' +
                ", manager='" + manager + '\'' +
                ", tags='" + tags + '\'' +
                ", village=" + village +
                ", schedules=" + schedules +
                ", dayRreports=" + dayRreports +
                ", weekRreports=" + weekRreports +
                ", monthRreports=" + monthRreports +
                '}';
    }
}
