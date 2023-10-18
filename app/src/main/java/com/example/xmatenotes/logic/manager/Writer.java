package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.logic.model.Page.IPage;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.logic.model.instruction.Calligraphy;
import com.example.xmatenotes.logic.model.instruction.Command;
import com.example.xmatenotes.logic.model.instruction.CommandDetector;
import com.example.xmatenotes.logic.model.instruction.DoubleClick;
import com.example.xmatenotes.logic.model.instruction.Responser;
import com.example.xmatenotes.logic.model.instruction.SingleClick;
import com.example.xmatenotes.logic.model.instruction.SymbolicCommand;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private WriteTimer writeTimer;

    private IPage page;

    private HandWriting handWritingBuffer = null;
    private SingleHandWriting singleHandWritingBuffer = null;
    //普通书写延时任务ID
    public ResponseWorker handWritingWorker = null;
    public static final String HANDWRITING_WORKER_NAME = "普通书写延时响应任务";
    //单次笔迹延时任务ID
    public ResponseWorker singleHandWritingWorker = null;

    public static final String SINGLEHANDWRITING_WORKER_NAME = "单次笔迹延时响应任务";

    //双击间隔计时器
    public ResponseWorker doubleClickPeriodWorker = null;
    public static final String DOUBLECLICK_PERIOD_WORKER_NAME = "双击间隔计时任务";

    //非动作命令延时识别计时器
    public ResponseWorker symbolicDelayWorker = null;
    public static final String SYMBOLIC_DELAY_WORKER_NAME = "非动作命令延时识别计时任务";
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
     * @param page
     * @return
     */
    public Writer bindPage(IPage page){
        this.page = page;
        if(this.page != null){
            LogUtil.e(TAG, "bindPage: Writer绑定Page: "+this.page.getCode());
        } else {
            LogUtil.e(TAG, "bindPage: Writer绑定Page: "+this.page);
        }

        writeTimer = new WriteTimer();
        new Thread(writeTimer).start();
        LogUtil.e(TAG, "bindPage: 延时计时器启动");
        return this;
    }

    /**
     * 解绑版面对象
     * @return
     */
    public Writer unBindPage(){
        if(this.page != null){
            LogUtil.e(TAG, "unBindPage: Writer解绑Page: "+this.page.getCode());
        } else {
            LogUtil.e(TAG, "unBindPage: Writer解绑Page: "+this.page);
        }
        this.page = null;
        if(writeTimer != null){
            writeTimer.stop();
        }
        return this;
    }

    public IPage getBindedPage(){
        if(this.page != null){
            return this.page;
        }

        LogUtil.e(TAG, "getBindedPage: 未绑定Page!");
        return null;
    }

//    private Writer setCommandDetector(CommandDetector commandDetector){
//        this.commandDetector = commandDetector;
//        return this;
//    }

    public Writer setResponser(Responser responser){
        this.responser = responser;
        LogUtil.e(TAG, "setResponser: 配置响应器");
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

        //如果有，移除普通书写延时响应任务
        if(containsResponseWorker(this.handWritingWorker)){
            deleteResponseWorker(this.handWritingWorker);
            LogUtil.e(TAG, "processEachDot: 移除普通书写延时响应任务");
        }

        //如果有，移除单次笔迹延时响应任务
        if(containsResponseWorker(this.singleHandWritingWorker)){
            deleteResponseWorker(this.singleHandWritingWorker);
            LogUtil.e(TAG, "processEachDot: 移除单次笔迹延时响应任务");
        }

        //如果有，移除非动作命令延时识别计时任务
        if(containsResponseWorker(this.symbolicDelayWorker)){
            deleteResponseWorker(this.symbolicDelayWorker);
            LogUtil.e(TAG, "processEachDot: 移除单次笔迹延时响应任务");
        }

        //如果有，移除双击间隔计时任务
        if(containsResponseWorker(this.doubleClickPeriodWorker)){
            deleteResponseWorker(this.doubleClickPeriodWorker);
            LogUtil.e(TAG, "processEachDot: 移除双击间隔计时任务");
        }

//        pageManager.update(mediaDot);

//        penMacManager.putMac(mediaDot.penMac);

        if(singleHandWritingBuffer == null){
            singleHandWritingBuffer = new SingleHandWriting();

            //设置进Page
            if(this.page != null){
                this.page.addSingleHandWriting(singleHandWritingBuffer);
                LogUtil.e(TAG, "processEachDot: 新的单次笔迹开始: page.addSingleHandWriting(singleHandWritingBuffer)");
            }
        }


        if(handWritingBuffer == null){
            if(lastDot == null){
                handWritingBuffer = new HandWriting(period, mediaDot.timelong);
            } else {
                handWritingBuffer = new HandWriting(mediaDot.timelong - lastDot.timelong, mediaDot.timelong);
            }
            if(singleHandWritingBuffer != null){
                singleHandWritingBuffer.addHandWriting(handWritingBuffer);
                LogUtil.e(TAG,"processEachDot: 添加新handWriting: singleHandWritingBuffer.addHandWriting(handWritingBuffer)");
            }
            LogUtil.e(TAG, "processEachDot: 新的普通书写开始");
        }

        if(handWritingBuffer.isEmpty() && lastDot != null){
            handWritingBuffer.setPrePeriod(mediaDot.timelong - lastDot.timelong);
        }

        handWritingBuffer.addDot(mediaDot);
        LogUtil.e(TAG, "processEachDot: 存入书写缓存：handWritingBuffer.addDot(mediaDot): "+mediaDot.toString());
        lastDot = mediaDot;

        if(commandDetector != null){
            if(!handWritingBuffer.isClosed()){
                commandDetector.setSymbolicCommandAvailable(false);
            }

            Command com = commandDetector.recognize(handWritingBuffer);
            if(com instanceof SingleClick && com.getHandWriting().isClosed()){
                //双击间隔计时器
                LogUtil.e(TAG, "processEachDot: 开启双击间隔计时器");
                this.updateStartTime();
                this.doubleClickPeriodWorker = addResponseWorker(DOUBLECLICK_PERIOD_WORKER_NAME, DoubleClick.DOUBLE_CLICK_PERIOD, new ResponseTask() {
                    @Override
                    public void execute() {
                        response(com);
                    }
                });
            } else if(com instanceof Calligraphy && com.getHandWriting().isClosed()){
                this.updateStartTime();
                response(com);

                this.symbolicDelayWorker = addResponseWorker(SYMBOLIC_DELAY_WORKER_NAME, SymbolicCommand.SYMBOLIC_DELAY, new ResponseTask() {
                    @Override
                    public void execute() {
                        commandDetector.setSymbolicCommandAvailable(true);
                        commandDetector.recognize(handWritingBuffer);

//                        response(comDelay);
                    }
                });
                LogUtil.e(TAG,"processEachDot: 开启非动作命令延时识别计时器");

                //普通书写基本延时响应
                this.handWritingWorker = addResponseWorker(HANDWRITING_WORKER_NAME,
                        HandWriting.DELAY_PERIOD, new ResponseTask() {
                            @Override
                            public void execute() {
                                closeHandWriting();
                                LogUtil.e(TAG, "processEachDot: close handWriting");
                                Command command = com.clone();
                                command.setName("DelayHandWriting");
                                response(command);
                            }
                        }
                );
                LogUtil.e(TAG,"processEachDot: 开启普通书写延时响应任务");

                this.singleHandWritingWorker = addResponseWorker(SINGLEHANDWRITING_WORKER_NAME,
                        SingleHandWriting.SINGLE_HANDWRITING_DELAY_PERIOD, new ResponseTask() {
                            @Override
                            public void execute() {
                                closeSingleHandWriting();
                                LogUtil.e(TAG, "processEachDot: close singleHandWriting");
                                Command command = com.clone();
                                command.setName("DelaySingleHandWriting");
                                response(command);
                            }
                        }
                );
                LogUtil.e(TAG,"processEachDot: 开启单次笔迹延时响应任务");
            } else {
                this.updateStartTime();
                response(com);
            }

        }
    }

    public void response(Command command){
        LogUtil.e(TAG, "response: 识别结束");
        if(command != null){
            if(command instanceof SymbolicCommand){
                closeHandWriting();
                if(this.singleHandWritingBuffer != null){
                    this.singleHandWritingBuffer.computeRect();
                }
                //如果有，移除普通书写延时响应任务
                if(containsResponseWorker(handWritingWorker)){
                    deleteResponseWorker(handWritingWorker);
                    LogUtil.e(TAG, "response: 移除普通书写延时响应任务");
                }

                //如果有，移除单次笔迹延时响应任务
                if(containsResponseWorker(singleHandWritingWorker)){
                    deleteResponseWorker(singleHandWritingWorker);
                    LogUtil.e(TAG, "response: 移除单次笔迹延时响应任务");
                }
            }

            command.addObserver(responser);
            LogUtil.e(TAG, "response: 开始响应");
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
                    LogUtil.e(TAG, "rectifyDot: 异常点：PEN_UP后直接出现PEN_MOVE");
                    mediaDot.type = Dot.DotType.PEN_DOWN;//可能存在PEN_UP后直接PEN_MOVE的情况
                }
            }
            if (mediaDot.type == Dot.DotType.PEN_UP) {
                if (lastDot.type == Dot.DotType.PEN_UP) {
                    LogUtil.e(TAG, "rectifyDot: 异常点：PEN_UP后接着出现PEN_UP");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 添加延时任务
     * @param name 标识名称，可为null
     * @param delay 延时时间
     * @param responseTask 延时任务
     * @return 任务，在需要的时候用来移除指定任务
     */
    public ResponseWorker addResponseWorker(String name, long delay, ResponseTask responseTask){
        return this.writeTimer.addResponseWorker(name, delay, responseTask);
    }

    /**
     * 移除指定延时任务
     * @param worker 任务
     * @return
     */
    public ResponseWorker deleteResponseWorker(ResponseWorker worker){
        return this.writeTimer.deleteResponseWorker(worker);
    }

    /**
     * 是否存在某任务
     * @param worker
     * @return
     */
    public boolean containsResponseWorker(ResponseWorker worker){
        return this.writeTimer.containsResponseWorker(worker);
    }

    /**
     * 在确定普通书写结束时调用
     */
    public void closeHandWriting(){
        if(handWritingBuffer != null){
            handWritingBuffer.close();
            handWritingBuffer = null;
            LogUtil.e(TAG, "closeHandWriting: 一次普通书写结束,handWritingBuffer = null");
        }
    }

    /**
     * 在确定单次笔迹结束时调用
     */
    public void closeSingleHandWriting(){
        if(singleHandWritingBuffer != null){
            singleHandWritingBuffer.close();
            singleHandWritingBuffer = null;
            LogUtil.e(TAG, "closeSingleHandWriting: 单次笔迹结束,singleHandWritingBuffer = null");
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

        private String name;

        /**
         * 起始时间
         */
        private long startTime;
       private long delay;

        /**
         * 是否有效
         */
        private boolean isAvailable;
       private ResponseTask responseTask;

        public ResponseWorker(int workerId, String name, long delay, ResponseTask responseTask) {
            this.startTime = System.currentTimeMillis();
            this.workerId = workerId;
            this.name = name;
            this.delay = delay;
            this.responseTask = responseTask;
            this.isAvailable = true;
        }

        public int getWorkerId() {
            return workerId;
        }

        public void setWorkerId(int workerId) {
            this.workerId = workerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getStartTime() {
            return startTime;
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

        public void setAvailable(boolean available) {
            isAvailable = available;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        @Override
        public int compareTo(ResponseWorker o) {
            return (int) ((this.startTime + this.delay) - (o.startTime + o.delay));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResponseWorker that = (ResponseWorker) o;
            return workerId == that.workerId && startTime == that.startTime && delay == that.delay && isAvailable == that.isAvailable && Objects.equals(name, that.name) && Objects.equals(responseTask, that.responseTask);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workerId, name, startTime, delay, isAvailable, responseTask);
        }

        @Override
        public String toString() {
            return "ResponseWorker{" +
                    "workerId=" + workerId +
                    ", name='" + name + '\'' +
                    ", startTime=" + startTime +
                    ", delay=" + delay +
                    ", isAvailable=" + isAvailable +
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

        private static final String TAG = "WriteTimer";

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

                synchronized (TAG){
                    while (!this.priorityQueue.isEmpty()){
//                        if(delay >= this.priorityQueue.peek().getDelay()){
                        ResponseWorker responseWorker = this.priorityQueue.peek();
                        if((System.currentTimeMillis() - responseWorker.getStartTime()) >= responseWorker.getDelay()){
//                            ResponseWorker responseWorker = this.priorityQueue.poll();
                            this.priorityQueue.poll();
                            this.responseWorkerMap.remove(responseWorker.getWorkerId());
                            responseWorker.getResponseTask().execute();
                            responseWorker.setAvailable(false);
                            LogUtil.e(TAG, "run: 执行了延时任务："+responseWorker);
                        }else {
                            break;
                        }
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
        public ResponseWorker addResponseWorker(String name, long delay, ResponseTask responseTask){
            synchronized (TAG){
                int workId = this.priorityQueue.size()+1;
                ResponseWorker responseWorker = new ResponseWorker(workId, name, delay, responseTask);
                this.priorityQueue.offer(responseWorker);
                this.responseWorkerMap.put(workId, responseWorker);
                LogUtil.e(TAG, "addResponseWorker: 添加延时任务："+ responseWorker);
                return responseWorker;
            }
        }

        /**
         * 移除指定延时任务
         * @param worker 延时任务
         * @return 目标任务
         */
        public ResponseWorker deleteResponseWorker(ResponseWorker worker){
            synchronized (TAG){
                if(worker != null && containsResponseWorker(worker)){
                    ResponseWorker responseWorker = this.responseWorkerMap.remove(worker.getWorkerId());
                    this.priorityQueue.remove(responseWorker);
                    LogUtil.e(TAG, "deleteResponseWorker: 移除指定延时任务："+responseWorker);
                    responseWorker.setAvailable(false);
                    return responseWorker;
                }
                return null;
            }
        }

        /**
         * 是否存在目标延时任务
         * @param worker
         * @return
         */
        public boolean containsResponseWorker(ResponseWorker worker){
//            return this.responseWorkerMap.containsKey(worker);
            return this.priorityQueue.contains(worker);
        }

        /**
         * 设置起始时间
         * @param startTime
         */
        public void setStartTime(long startTime) {
            this.startTime = startTime;
            LogUtil.e(TAG, "setStartTime: 变更延时计时器起始时间为："+startTime);
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
            this.priorityQueue.clear();
            this.responseWorkerMap.clear();
            LogUtil.e(TAG, "stop: 延时计时器关闭");
        }
    }

}
