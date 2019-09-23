package com.ming.homwork.bean;

import cn.bmob.v3.BmobObject;

/**
 * @author luoming
 *created at 2019-09-09 14:26
 * 项目表
 *
*/
public class ProjectTab extends BmobObject {
    private int id;
    private String prijectName;
    private boolean isEnable;

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrijectName() {
        return prijectName;
    }

    public void setPrijectName(String prijectName) {
        this.prijectName = prijectName;
    }


    public int getId() {
        return id;
    }
}
