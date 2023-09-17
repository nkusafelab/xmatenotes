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

    public CircleRunnable(CircleCallBack circleCallBack) {
        this.circleCallBack = circleCallBack;
    }

    @Override
    public void run() {
        while (true){
            if(isStart){
                if(circleCallBack != null){
                    circleCallBack.stopableCallBack();
                }
            }

            if(circleCallBack != null){
                circleCallBack.circleCallBack();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 判断线程是否尚未终止
     * @return
     */
    public boolean isAlive(){
        return isStart;
    }

    /**
     * 终止线程执行
     */
    public void stop(){
        isStart = false;
    }
}
