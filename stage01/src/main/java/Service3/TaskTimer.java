package Service3;

/**
 * TODO Timer的作用？
 *  给任务计算时间。由于timer是一个个创建的，所有形成了先后队列。
 */
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
