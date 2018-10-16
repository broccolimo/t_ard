package com.yzmc.model;

public class OrderModel {
    public int code;
    public String msg;
    public OrderInfo obj;

    public static class OrderInfo{
        public double jing;
        public double wei;
        public String addr;
        public String productID;
        public String maintain_id;
        public int maintain_type;
    }
}
