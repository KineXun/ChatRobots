package com.example.chatrobot01;

//数据集合类
public class MyDataSet {

    private String place;//位置
    private String data;//文本数据

    public void setPlace(String place){
        this.place = place;
    }

    public void setData(String data){
        this.data = data;
    }

    public String getPlace(){
        return this.place;
    }

    public String getData(){
        return this.data;
    }

    public MyDataSet(String place,String data){
        this.place = place;
        this.data = data;
    }

}
