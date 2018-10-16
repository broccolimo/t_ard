package com.yzmc.util;

import java.util.HashMap;
import java.util.Map;

public class FaultCode {
    private final static Map<Integer, String> map = new HashMap<>();

    static{
        map.put(1, "安全回路断路 ");
        map.put(2, "开门故障");
        map.put(3, "关门故障");
        map.put(32, "驱动器过流故障");
        map.put(34, "散热器过温");
        map.put(35, "马达过载");
        map.put(36, "驱动器过载");
        map.put(37, "直流母线过压");
        map.put(38, "直流母线欠压");
        map.put(39, "主机超速");
        map.put(40, "编码器丢失故障");
        map.put(58, "抱闸开关1检测故障");
        map.put(60, "三相输入电源欠压");
        map.put(63, "主接触器或抱闸接触器动作故障");
        map.put(64, "平层光电开关故障");
        map.put(65, "抱闸开关2检测故障");
        map.put(66, "抱闸制动力错误");
        map.put(75, "抱闸制动力检测警告");
        map.put(76, "抱闸制动力检测故障");
        map.put(93, "主电源丢失");
        map.put(95, "钢丝绳滑移");
        map.put(109, "主机热敏故障");
        map.put(158, "超载模式");

        map.put(6, "门区外停梯");
        map.put(7, "门锁回路断路");
        map.put(9, "电梯超速");
        map.put(10, "困人");
        map.put(11, "冲顶");
        map.put(12, "蹲底");
        map.put(107, "轿厢意外移动");
    }

    public static String getFaultName(int faultcode){
        return map.get(faultcode);
    }
}
