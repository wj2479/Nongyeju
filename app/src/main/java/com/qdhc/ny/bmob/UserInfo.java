package com.qdhc.ny.bmob;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 用户账号
 */
public class UserInfo extends BmobUser implements Serializable {

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 年龄
     */
    private int age;

    /**
     * 性别
     */
    private int gender;

    /**
     * 头像
     */
    private BmobFile avatar;
    /**
     * 角色ID
     */
    private int role;

    /**
     * 所属乡镇
     */
    private String district;

    /**
     * 管辖区县ID
     */
    private int areaId;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "nickName='" + nickName + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", avatar=" + avatar +
                ", role=" + role +
                ", district='" + district + '\'' +
                ", areaId=" + areaId +
                '}';
    }
}
