package com.example.xmatenotes.ui.qrcode;

public class CircleRunnable implements Runnable {

    interface CircleCallBack {
        /**
         * 可能会停止执行
         */
        public void stopableCallBack();

        /**
         * 不会停止执行，除非线程销毁
         */
        public void circleCallBack();
    }

    private CircleCallBack circleCallBack;

    //控制线程是否终止运行
    private boolean isStart = true;

    //控制可停止部分是否终止运行
    private boolean isStopableCallBackStart = true;

    /**
     * 单次循环睡眠时间
     */
    private long sleepTime = 1000;

    public CircleRunnable(CircleCallBack circleCallBack) {
        this.circleCallBack = circleCallBack;
    }

    @Override
    public void run() {
        while (isStart){
            if(isStopableCallBackStart){
                if(circleCallBack != null){
                    circleCallBack.stopableCallBack();
                }
            }

            if(circleCallBack != null){
                circleCallBack.circleCallBack();
            }

            if(sleepTime > 0){
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     *
     * @param sleepTime 小于等于0表示每次循环不睡眠
     * @return
     */
    public CircleRunnable setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    /**
     * 判断线程是否尚未终止
     * @return
     */
    public boolean isAlive(){
        return isStart;
    }

    /**
     * 判断可停止部分是否已经停止
     * @return
     */
    public boolean isStopableCallBackStart() {
        return isStopableCallBackStart;
    }

    /**
     * 终止线程执行
     */
    public void stop(){
        isStart = false;
    }

    /**
     * 终止stopable部分的执行
     */
    public void stopPart(){
        isStopableCallBackStart = false;
    }
}
