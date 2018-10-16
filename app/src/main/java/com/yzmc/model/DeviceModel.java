package com.yzmc.model;

public class DeviceModel {
    public int code;
    public String msg;
    public DeviceInfo obj;

    public static class DeviceInfo{
        public String useUnitID;
        public String productID;
        public String makeUnit;
        public String installUnit;
        public String repairUnit;
        public String maintainUnit;
        public String elevatorModel;
        public String motorPower;
        public String controlBox;
        public String brakes;
        public String speedLimiter;
        public String safetyGear;
        public String doorLock;
        public String ratedSpeed;
        public String ratedLoad;
        public String transformModel;
        public String annualInspection;
        public tractionMachineInfo tractionMachine;
        public layersInfo layers;
    }

    public static class tractionMachineInfo{
        public String num;
        public String diameter;
    }

    public static class layersInfo{
        public String floor;
        public String station;
        public String door;
    }
}
