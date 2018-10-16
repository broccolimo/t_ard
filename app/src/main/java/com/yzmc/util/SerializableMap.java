package com.yzmc.util;

import java.io.Serializable;
import java.util.Map;

public class SerializableMap implements Serializable {
    private Map<String, Object> map;

    public void setMap(Map<String, Object> map){
        this.map = map;
    }

    public Map<String, Object> getMap(){
        return map;
    }
}
