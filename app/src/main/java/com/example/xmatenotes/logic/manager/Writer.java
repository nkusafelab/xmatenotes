package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.App.XmateNotesApplication;
import com.example.xmatenotes.logic.model.Page.Card;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.logic.model.instruction.Command;
import com.example.xmatenotes.logic.model.instruction.CommandDetector;
import com.example.xmatenotes.logic.model.instruction.Responser;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 统一管理版面的书写相关操作，包括命令响应
 */
public class Writer {

    private static final String TAG = "Writer";

    private static final Writer writer = new Writer();

    private static PageManager pageManager;
    private static PenMacManager penMacManager;
    private static AudioManager audioManager;
    private static ExcelReader excelReader;
    private CommandDetector commandDetector;
    private Responser responser;
    private CoordinateConverter coordinateConverter;
    private WriteTimer writeTimer;

    private Card cardData;

    private HandWriting handWritingBuffer = null;
    private SingleHandWriting singleHandWritingBuffer = null;
    //普通书写延时任务ID
    public int handWritingWorkerId = XmateNotesApplication.DEFAULT_INT;
    //单次笔迹延时任务ID
    public int singleHandWritingWorkerId = XmateNotesApplication.DEFAULT_INT;

    private long period = Long.MAX_VALUE;
    private MediaDot lastDot;

    private Writer(){
    }

    public static Writer getInstance(){
        return writer;
    }

    /**
     * 进入新活动时调用进行初始化
     */
    public Writer init(){
        pageManager = PageManager.getInstance();
        penMacManager = PenMacManager.getInstance();
        audioManager = AudioManager.getInstance();
        excelReader = ExcelReader.getInstance();
        this.commandDetector = new CommandDetector(this);
        return this;
    }

    /**
     * 绑定版面对象
     * @param cardData
     * @return
     */
    public Writer bindCard(Card cardData){
        this.cardData = cardData;
        LogUtil.e(TAG, "Writer绑定Card");
        writeTimer = new WriteTimer();
        new Thread(writeTimer).start();
        LogUtil.e(TAG, "延时计时器启动");
        return this;
    }

    /**
     * 解绑版面对象
     * @return
     */
    public Writer unBindCard(){
        this.cardData = null;
        LogUtil.e(TAG, "Writer解绑Card");
        writeTimer.stop();
        return this;
    }

    private Writer setCommandDetector(CommandDetector commandDetector){
        this.commandDetector = commandDetector;
        return this;
    }

    public Writer setResponser(Responser responser){
        this.responser = responser;
        return this;
    }

    /**
     * 设置UI坐标到真实物理坐标的转换参数
     * @param coordinateConverter
     * @return
     */
    public Writer setCoordinateConverter(CoordinateConverter coordinateConverter){
        this.coordinateConverter = coordinateConverter;
        LogUtil.e(TAG, "配置坐标转换器");
        return this;
    }

    /**
     * 设置延时计时器起始时间
     * @param startTime
     */
    public void setStartTime(long startTime) {
        this.writeTimer.setStartTime(startTime);
    }

    /**
     * 设置延时计时器起始时间为当前时间
     */
    public void updateStartTime(){
        setStartTime(System.currentTimeMillis());
    }

    /**
     * 对每个接收到的笔迹点进行处理。
     * 当前在哪个活动界面书写，就在哪个活动中调用该方法传入笔迹点进行处理
     * @param mediaDot 待处理mediaDot
     */
    public void processEachDot(MediaDot mediaDot){

        //检查和处理异常点
        if(!rectifyDot(mediaDot)){
            return;
        }

        if(containsResponseWorker(this.handWritingWorkerId)){
            deleteResponseWorker(this.handWritingWorkerId);
        }

        if(containsResponseWorker(this.singleHandWritingWorkerId)){
            deleteResponseWorker(this.singleHandWritingWorkerId);
        }

        MediaDot inMediaDot = mediaDot;
        //将UI坐标转换为内部真实物理坐标
        if(coordinateConverter != null){
            inMediaDot = coordinateConverter.convertIn(mediaDot);
        }

//        pageManager.update(mediaDot);

//        penMacManager.putMac(mediaDot.penMac);

        if(singleHandWritingBuffer == null){
            singleHandWritingBuffer = new SingleHandWriting();

            //设置进card
            if(this.cardData != null){
                this.cardData.addSingleHandWriting(singleHandWritingBuffer);
            }
        }

        if(handWritingBuffer.isEmpty()){
            handWritingBuffer.setPrePeriod(mediaDot.timelong - lastDot.timelong);
        }

        if(handWritingBuffer == null){
            if(lastDot == null){
                handWritingBuffer = new HandWriting(period);
            } else {
                handWritingBuffer = new HandWriting(mediaDot.timelong - lastDot.timelong);
            }
            if(singleHandWritingBuffer != null){
                singleHandWritingBuffer.addHandWriting(handWritingBuffer);
            }
        }

        handWritingBuffer.addDot(inMediaDot);

        if(commandDetector != null){
            if(!handWritingBuffer.isClosed()){
                commandDetector.setSymbolicCommandAvailable(false);
            }
            response(commandDetector.recognize(handWritingBuffer));
        }
    }

    public void response(Command command){
        if(command != null){
            command.addObserver(responser);
            command.response();
        }
    }

    /**
     * 检查和处理可能的异常点，确保点的参数正确
     * @param mediaDot
     * @return 若返回true，可继续处理该点;若返回false，不可继续处理该点
     */
    private boolean rectifyDot(MediaDot mediaDot) {
        if (lastDot != null) {
            if (mediaDot.type == Dot.DotType.PEN_MOVE) {
                if (lastDot.type == Dot.DotType.PEN_UP) {
                    mediaDot.type = Dot.DotType.PEN_DOWN;//可能存在PEN_UP后直接PEN_MOVE的情况
                }
            }
            if (mediaDot.type == Dot.DotType.PEN_UP) {
                if (lastDot.type == Dot.DotType.PEN_UP) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 添加延时任务
     * @param delay 延时时间
     * @param responseTask 延时任务
     * @return 任务Id，在需要的时候用来移除指定任务
     */
    public int addResponseWorker(long delay, ResponseTask responseTask){
        return this.writeTimer.addResponseWorker(delay, responseTask);
    }

    /**
     * 移除指定延时任务
     * @param workerId 任务Id
     * @return
     */
    public ResponseWorker deleteResponseWorker(int workerId){
        return this.writeTimer.deleteResponseWorker(workerId);
    }

    /**
     * 是否存在某任务
     * @param workerId
     * @return
     */
    public boolean containsResponseWorker(int workerId){
        return this.writeTimer.containsResponseWorker(workerId);
    }

    /**
     * 在确定普通书写结束时调用
     */
    public void closeHandWriting(){
        if(handWritingBuffer != null){
            handWritingBuffer = null;
        }
    }

    /**
     * 在确定单次笔迹结束时调用
     */
    public void closeSingleHandWriting(){
        if(singleHandWritingBuffer != null){
            singleHandWritingBuffer.close();
            singleHandWritingBuffer = null;
        }
    }

    /**
     * 一次书写完成时调用
     */
    public void close(){
        singleHandWritingBuffer.close();
        singleHandWritingBuffer = null;
        handWritingBuffer = null;
    }

    /**
     * 延时任务
     */
    public class ResponseWorker implements Comparable<ResponseWorker> {
        private int workerId;
       private long delay;
       private ResponseTask responseTask;

        public ResponseWorker(int workerId, long delay, ResponseTask responseTask) {
            this.workerId = workerId;
            this.delay = delay;
            this.responseTask = responseTask;
        }

        public int getWorkerId() {
            return workerId;
        }

        public void setWorkerId(int workerId) {
            this.workerId = workerId;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public ResponseTask getResponseTask() {
            return responseTask;
        }

        public void setResponseTask(ResponseTask responseTask) {
            this.responseTask = responseTask;
        }

        @Override
        public int compareTo(ResponseWorker o) {
            return (int) (this.delay - o.delay);
        }

        @Override
        public String toString() {
            return "ResponseWorker{" +
                    "workerId=" + workerId +
                    ", delay=" + delay +
                    ", responseTask=" + responseTask +
                    '}';
        }
    }

    /**
     * 任务执行实体
     */
     public interface ResponseTask {
        public void execute();
    }

    /**
     * 多延时任务计时器
     * 先设置startTime，再添加延时任务
     */
    public class WriteTimer implements Runnable {

        //控制线程是否终止运行
        private boolean isStart = true;

        /**
         * 延时起始时间
         */
        private long startTime = 0;

        private long delay = 0;

        /**
         * 存储任务id到任务的映射
         */
        private Map<Integer, ResponseWorker> responseWorkerMap = new HashMap<>();

        //任务优先队列
        private PriorityQueue<ResponseWorker> priorityQueue = new PriorityQueue<>();

        @Override
        public void run() {
            while (isStart) {
                this.delay = System.currentTimeMillis() - this.startTime;

                while (!this.priorityQueue.isEmpty()){
                    if(delay >= this.priorityQueue.peek().getDelay()){
                        ResponseWorker responseWorker = this.priorityQueue.poll();
                        this.responseWorkerMap.remove(responseWorker.getWorkerId());
                        responseWorker.getResponseTask().execute();
                        LogUtil.e(TAG, "执行了延时任务："+responseWorker);
                    }else {
                        break;
                    }
                }
            }

        }

        /**
         * 添加延时任务
         * @param delay 延时时间
         * @param responseTask 延时任务
         * @return 任务id，可以用来在需要的时候删除指定的任务
         */
        public int addResponseWorker(long delay, ResponseTask responseTask){
            int workId = this.priorityQueue.size()+1;
            ResponseWorker responseWorker = new ResponseWorker(workId, delay, responseTask);
            this.priorityQueue.offer(responseWorker);
            this.responseWorkerMap.put(workId, responseWorker);
            LogUtil.e(TAG, "添加延时任务："+ responseWorker);
            return workId;
        }

        /**
         * 移除指定延时任务
         * @param workerId 延时任务Id
         * @return 目标任务
         */
        public ResponseWorker deleteResponseWorker(int workerId){
            if(containsResponseWorker(workerId)){
                ResponseWorker responseWorker = this.responseWorkerMap.remove(workerId);
                this.priorityQueue.remove(responseWorker);
                LogUtil.e(TAG, "移除指定延时任务："+responseWorker.toString());
                return responseWorker;
            }
            return null;
        }

        /**
         * 是否存在目标延时任务
         * @param worker
         * @return
         */
        public boolean containsResponseWorker(int worker){
            return this.responseWorkerMap.containsKey(worker);
        }

        /**
         * 设置起始时间
         * @param startTime
         */
        public void setStartTime(long startTime) {
            this.startTime = startTime;
            LogUtil.e(TAG, "变更延时计时器起始时间为："+startTime);
        }

        public long getDelay() {
            return delay;
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
            LogUtil.e(TAG, "延时计时器关闭");
        }
    }

}
