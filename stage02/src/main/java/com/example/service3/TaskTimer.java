package com.example.service3;

public class TaskTimer implements Runnable{
    private int nowTime = 0;

    @Override
    public void run() {
        nowTime++;
    }

    public int getTime() {
        return nowTime;
    }
}
