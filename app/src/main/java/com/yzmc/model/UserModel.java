package com.yzmc.model;

public class UserModel {

    public int code;
    public String msg;
    public UserInfo obj;

    public static class UserInfo{
        public boolean canAccept_b;
        public boolean canAccept_x;
        public String account;
        public String name;
        public String phone;
        public int role;
        public String company;
    }


}
