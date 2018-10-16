package com.yzmc.model;

public class XOrderArrayModel {
    public int code;
    public String msg;
    public xorderInfo[] obj;
    public Object err;


    public static class xorderInfo{
        public String xorder_id;
        public String useUnit;
        public String addr;
        public String registerID;
        public String elevatorModel;
        public String release_time;
        public int faultCode;
        public int flag;
    }
}
