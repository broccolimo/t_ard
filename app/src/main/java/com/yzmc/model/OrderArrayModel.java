package com.yzmc.model;

public class OrderArrayModel {
    public int code;
    public String msg;
    public OrderInfo[] obj;

    public static class OrderInfo{
        public String order_id;
        public String useUnit;
        public String addr;
        public String registerID;
        public String productID;
        public String release_time;
        public int maintain_type;
        public int flag;
        public String refuse_reason;
        public String delay_description;
        public String refuse_time;
        public String delay_time;
        public Double jing;
        public Double wei;
    }
}
