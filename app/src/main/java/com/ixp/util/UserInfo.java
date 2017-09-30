package com.ixp.util;

import org.json.JSONObject;


public class UserInfo {

    public String username;
    public String password;
    public String userNo;
    public String isAdmin;
    public String createDate;
    public String mail;
    public int logout;

    public static UserInfo createFromJson(String str) {
        try {
            JSONObject object = new JSONObject(str);
            UserInfo userInfo = new UserInfo();

            JSONObject obj = object.getJSONObject("admin");
            userInfo.username = obj.getString("username");
            userInfo.password = obj.getString("password");
            userInfo.userNo = obj.getString("userNo");
            userInfo.isAdmin = obj.getString("isAdmin");
            userInfo.createDate = obj.getString("createDate");
            userInfo.mail = obj.getString("mail");
            return userInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", userNo='" + userNo + '\'' +
                ", isAdmin='" + isAdmin + '\'' +
                ", createDate='" + createDate + '\'' +
                ", mail='" + mail + '\'' +
                '}';
    }
}
