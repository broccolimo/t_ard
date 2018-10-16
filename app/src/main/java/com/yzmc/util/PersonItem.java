package com.yzmc.util;

public class PersonItem {
    private String name;
    private String account;
    public PersonItem(String name, String account){
        this.name = name;
        this.account = account;
    }

    public String getName(){
        return name;
    }

    public String getAccount(){
        return account;
    }

    public String toString(){
        return name;
    }

}
