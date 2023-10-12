package com.example.xmatenotes.logic.model.Page;

import android.graphics.RectF;
import android.util.Log;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.manager.CoordinateConverter;
import com.example.xmatenotes.logic.manager.OldPageManager;
import com.example.xmatenotes.logic.model.Role;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.SerializableRectF;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.util.LogUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p><strong>版面基类</strong></p>
 * <p></p>
 * <p>真实版面到计算设备中版面存储结构的映射，包含了每个版面必要的数据结构、属性特征和操作它们的相关方法</p>
 * @see OldPageManager
 */
public class Page implements IPage,Serializable {

    private static final String TAG = "Page";

    private static final long serialVersionUID = 3252412048548873077L;

    private static final String DATE_FORMAT = "yyyy-MMdd-hhmmss";

    /**
     * 飞书多维表格存储tableId
     */
    public static final String pagesTableId = "tblXcJERkVDkcPki";

    /**
     * 版面唯一标识符编码
     */
    protected String code;

    /**
     * 前置编码
     */
    protected String preCode;

    /**
     * 后置编码
     */
    protected String postCode;

    /**
     * 新迭代版本中笔迹的最小矩形left
     */
    protected float left = Float.MAX_VALUE;

    /**
     * 新迭代版本中笔迹的最小矩形top
     */
    protected float top = Float.MAX_VALUE;

    /**
     * 新迭代版本中笔迹的最小矩形right
     */
    protected float right = Float.MIN_VALUE;

    /**
     * 新迭代版本中笔迹的最小矩形bottom
     */
    protected float bottom = Float.MIN_VALUE;

    /**
     * 真实物理版面左边界在父版面坐标系中的横坐标，该版面中所有坐标点横坐标减去该值为该版面坐标系中的横坐标
     */
    protected float realLeft = 0F;

    /**
     * 真实物理版面上边界在父版面坐标系中的纵坐标，该版面中所有坐标点纵坐标减去该值为该版面坐标系中的纵坐标
     */
    protected float realTop = 0F;

    /**
     * 真实物理版面宽度，10-1mm，标志该版面存储的坐标点的横坐标范围
     */
    protected float realWidth;

    /**
     * 真实物理版面高度，10-1mm，标志该版面存储的坐标点的纵坐标范围
     */
    protected float realHeight;


    /**
     * 创建时间
     */
    protected String createTime = "-1";

    /**
     * 版面存储完整名称或相对路径
     */
    protected String pageStorageName;

    /**
     * 数据创建角色
     */
    private String role = "组长";

    /**
     * 学期
     */
    private String term = "秋季学期";

    /**
     * 学科
     */
    private String subjectName = "语文";

    /**
     * 单元名
     */
    private String unitName = "一元一次不等式";

    /**
     * 阶段名
     */
    private String stageName = "整体认知构建";

    /**
     * 版面上的音频文件名，不含后缀
     */
    private ArrayList<String> audioNameList = new ArrayList<>();

    /**
     * 笔迹数据
     */
    private ArrayList<SingleHandWriting> dotList = new ArrayList<>();

    private CoordinateConverter.CoordinateCropper coordinateCropper = null;

    private QRObject qrObject = new QRObject(
            "01",
            2290,
            1700,
            "1",
            60,
            60,
            270,
            "01",
            "01",
            "01",
            "2100_150",
            "011",
            "037",
            "01",
            "01",
            "6",
            "01",
            "01",
            "2023-0911-113300"
    );

    public Page() {
        this.coordinateCropper = new CoordinateConverter.CoordinateCropper(this.realLeft, this.realTop);
    }

    /**
     * 添加单次笔迹
     * @param singleHandWriting
     * @return
     */
    @Override
    public Page addSingleHandWriting(SingleHandWriting singleHandWriting) {
        this.dotList.add(singleHandWriting);
        LogUtil.e(TAG, "addSingleHandWriting: dotList.add(singleHandWriting)");
        return this;
    }

    public SingleHandWriting getLastSingleHandWriting(){
        if(this.dotList.size() > 0){
            return this.dotList.get(this.dotList.size()-1);
        } else {
            LogUtil.e(TAG, "getLastSingleHandWriting(): dotList为空！");
            return null;
        }
    }

    public Page addDotsList(int index, ArrayList<SingleHandWriting> singleHandWritingList){
        this.dotList.addAll(index, singleHandWritingList);
        return this;
    }

    public ArrayList<SingleHandWriting> getDotList() {
        return dotList;
    }

    public SimpleDot getLastDot(){
        if (!dotList.isEmpty()){
            return dotList.get(dotList.size()-1).getLastDot();
        }
        LogUtil.e(TAG, "getLastDot: dotList为空！");
        return null;
    }

    public Page addAudioNameList(int index, ArrayList<String> audioNameList){
        this.audioNameList.addAll(index, audioNameList);
        return this;
    }

    /**
     * 生成一个新音频文件名，不含后缀，存入audioNameList，并返回
     * @return
     */
    public String getNewAudioName(){
        String newAudioName = String.valueOf(this.audioNameList.size());
        this.audioNameList.add(newAudioName);
        LogUtil.e(TAG, "getNewAudioName: audioNameList.size为: "+this.audioNameList.size());
        return newAudioName;
    }

    public ArrayList<String> getAudioNameList() {
        return audioNameList;
    }

    public String getLastAudioName(){
        int audioNameListSize = this.audioNameList.size();
        if(audioNameListSize > 0){
            String lastAudioName = this.audioNameList.get(audioNameListSize-1);
            LogUtil.e(TAG, "getLastAudioName: "+lastAudioName);
            return lastAudioName;
        } else {
            LogUtil.e(TAG, "getLastAudioName: audioNameList为空！");
            return null;
        }
    }

    public HandWriting getHandWritingByCoordinate(SimpleDot dot){
        for(SingleHandWriting singleHandWriting : this.dotList){
            for(HandWriting handWriting : singleHandWriting.getHandWritings()){
                if(handWriting.contains(dot)){
                    return handWriting;
                }
            }
        }
        LogUtil.e(TAG, "getHandWritingByCoordinate(): 未找到目标坐标上的笔迹！");
        return null;
    }

    /**
     * 根据坐标获取可能叠加的音频，若无音频，返回null
     */
    public String getAudioNameByCoordinate(SimpleDot dot){
        for(SingleHandWriting singleHandWriting : this.dotList){
            for(HandWriting handWriting : singleHandWriting.getHandWritings()){
                LogUtil.e(TAG, "getAudioNameByCoordinate: handWriting.audioId: "+handWriting.getAudioId());
                if(handWriting.getAudioId() != XmateNotesApplication.DEFAULT_INT && handWriting.contains(dot)){
                    LogUtil.e(TAG, "getAudioNameByCoordinate: 坐标上叠加的音频为: "+handWriting.getAudioId());
                    return String.valueOf(handWriting.getAudioId());
                }
            }
        }
        LogUtil.e(TAG, "getAudioNameByCoordinate: 未找到目标坐标上的音频！");
        return null;
    }

    /**
     * 检查坐标值是否合法
     * @param dot
     * @return
     */
    private boolean checkDot(SimpleDot dot){
        if(dot.getIntX()<0 || dot.getIntX()>= this.realWidth || dot.getIntY()<0 || dot.getIntY()>=this.realHeight){
            Log.e(TAG,"目标点非法!");
            return false;
        }
        return true;
    }

    public boolean contains(SimpleDot dot){
        if(!checkDot(dot)){
            return false;
        }
        for(SingleHandWriting singleHandWriting : this.dotList){
            for(HandWriting handWriting : singleHandWriting.getHandWritings()){
                if(handWriting.contains(dot)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置版面左上角位置
     */
    public void setPosition(float realLeft, float realTop){
        this.realLeft = realLeft;
        this.realTop = realTop;
        this.coordinateCropper.setLeft(this.realLeft);
        this.coordinateCropper.setTop(this.realTop);
    }

    /**
     * 配置版面真实物理尺寸，单位10-1mm
     * @param realWidth
     * @param realHeight
     * @return
     */
    public Page setRealDimensions(float realWidth, float realHeight){
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        this.qrObject.setPsx((int) realWidth);
        this.qrObject.setPsy((int) realHeight);
        LogUtil.e(TAG, "setDimensions: 配置真实物理尺寸为 realWidth: "+this.realWidth+" realHeight: "+this.realHeight);
        return this;
    }

    public Page setRealDimensions(float showWidth, float showHeight, float dpi){
        setRealDimensions((showWidth / dpi) *254, (showHeight / dpi) *254);
        return this;
    }

    public CoordinateConverter.CoordinateCropper getCoordinateCropper() {
        return coordinateCropper;
    }

    public String getPageName(){
        this.pageStorageName = getPageStorageName(this.code, this.createTime);
        return this.pageStorageName;
    }

    public RectF getHandWritingsRectF(){
        return new RectF(this.left, this.top, this.right, this.bottom);
    }

    public RectF getPageBounds(){
        return new RectF(this.realLeft, this.realTop, this.realLeft+this.realWidth, this.realTop+this.realHeight);
    }

    /**
     * 获得版面对象存储文件夹名
     * @param code 4位编码
     * @param timelong
     * @return
     */
    public static String getPageStorageName(String code, String timelong){
        //版面类型+唯一标识符+生成时间
        return TAG+"#"+code+"#"+getFormatTime(Long.parseLong(timelong));
    }

    public static String getFormatTime(long timelong){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        Date dateName = new Date(timelong);
        return sdf.format(dateName);
    }

    /**
     * 获取笔迹人数
     * @return
     */
    public int getPeopleNum(){
        return 0;
    }

    /**
     * 获取笔迹次数
     * @return
     */
    public int getHandWritingsNum(){
        return 0;
    }

    /**
     * 根据目标点坐标获取所在HandWriting
     * @param dot
     * @return 如果存在目标点，则返回所在HandWriting; 否则返回null
     */
    public HandWriting getHandWritingByXY(SimpleDot dot){
        for(SingleHandWriting singleHandWriting : this.dotList){
            for(HandWriting handWriting : singleHandWriting.getHandWritings()){
                if(handWriting.contains(dot)){
                    return handWriting;
                }
            }
        }
        return null;
    }

    /**
     * 生成一个Page版本和二维码版本
     */
    public void create(){

        LogUtil.e(TAG, "create(): 原始getHandWritingsRectF():"+getHandWritingsRectF());
        LogUtil.e(TAG, "create(): 已有singleHandWriting数量: "+this.dotList.size());
        SerializableRectF rectF = new SerializableRectF();
        for (SingleHandWriting singleHandWriting : this.dotList){
            if(singleHandWriting.getBoundRectF().equals(rectF) || !singleHandWriting.isNew()){
                //若为空，或旧笔迹，直接跳过
                continue;
            }
            LogUtil.e(TAG, "create(): 前getHandWritingsRectF():"+getHandWritingsRectF());
            LogUtil.e(TAG, "create(): singleHandWriting.boundRectF: "+singleHandWriting.getBoundRectF());
            if(this.left > singleHandWriting.getBoundRectF().left){
                this.left = singleHandWriting.getBoundRectF().left;
            }
            if(this.top > singleHandWriting.getBoundRectF().top){
                this.top = singleHandWriting.getBoundRectF().top;
            }
            if(this.right < singleHandWriting.getBoundRectF().right){
                this.right = singleHandWriting.getBoundRectF().right;
            }
            if(this.bottom < singleHandWriting.getBoundRectF().bottom){
                this.bottom = singleHandWriting.getBoundRectF().bottom;
            }
            LogUtil.e(TAG, "create(): 后getHandWritingsRectF():"+getHandWritingsRectF());
        }
        LogUtil.e(TAG, "create(): 生成RectF: "+getHandWritingsRectF());

        this.createTime = String.valueOf(System.currentTimeMillis());
        this.qrObject.setTime(this.createTime);
        LogUtil.e(TAG, "create(): 生成时间: "+this.createTime);

        this.code = this.code.substring(0,6) + this.createTime;
        this.qrObject.setPn(this.code);
        LogUtil.e(TAG, "create()(): 生成code: "+this.code);
    }

    public QRObject toQRObject(){
        this.create();
        return this.qrObject;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("Code", this.code);
        map.put("preCode", this.preCode);
        map.put("postCode", this.postCode);
        map.put("数据创建角色", this.role);
        map.put("学校", this.qrObject.getSc());
        map.put("年级", this.qrObject.getGr());
        map.put("班级", this.qrObject.getCl());
        map.put("学期", this.term);
        map.put("版面类别", this.qrObject.getP());
        map.put("教师编号", this.qrObject.getTe());
        map.put("学生编号", this.qrObject.getSt());
        map.put("小组编号", this.qrObject.getGn());
        map.put("小组组型", this.qrObject.getGl());
        map.put("学科", this.subjectName);
        map.put("单元", this.unitName);
        map.put("Left", this.left);
        map.put("Top", this.top);
        map.put("Right", this.right);
        map.put("Bottom", this.bottom);
        map.put("卡片名称", this.getPageName());
        map.put("更新时间", Long.parseLong(this.createTime));

        return map;
    }

    public String getPreCode() {
        return preCode;
    }

    public void setPreCode(String preCode) {
        this.preCode = preCode;
        this.code = this.preCode;
        LogUtil.e(TAG, "setPreCode(): 设置preCode:"+this.preCode);
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
        LogUtil.e(TAG, "setPostCode(): 设置postCode:"+this.postCode);
    }

    /**
     * 设置新迭代版本
     * @param oldIteration
     */
    public void setIteration(String oldIteration){
        String newIteration = String.valueOf((Integer.parseInt(oldIteration) + 1));
        this.qrObject.setData(newIteration);
        LogUtil.e(TAG, "setIteration( newIteration: "+newIteration+" )");
    }

    /**
     * 设置二维码位置
     * @param left
     * @param top
     * @param length
     */
    public void setQRCodeRect(int left, int top, int length){
        this.qrObject.setQx(left);
        this.qrObject.setQy(top);
        this.qrObject.setQl(length);
        LogUtil.e(TAG, "setQRCodeRect( left:"+left+", top:"+top+", length:"+length+" )");
    }
    /**
     * 更新角色身份信息
     * @param role
     */
    public void updateRole(Role role){
        //角色
        this.role = role.getRoleName();
        //学生编号
        this.qrObject.setSt(role.getStudentNumber());
        //小组编号
        this.qrObject.setGn(role.getGroupNumber());
        //小组组型
        this.qrObject.setGl(role.getGroupTip());
        //学校
        this.qrObject.setSc(role.getSchool());
        //班级
        this.qrObject.setCl(role.getClassNumber());
        //年级
        this.qrObject.setGr(role.getGrade());
        LogUtil.e(TAG, "updateRole: "+role);
    }

    public Role getRole(){
        return new Role(this.role, this.qrObject.getSt(), this.qrObject.getGn(), this.qrObject.getGl(),this.qrObject.getSc(), this.qrObject.getCl(), this.qrObject.getGr(), XmateNotesApplication.mBTMac);
    }

    public String getPageStorageName() {
        return pageStorageName;
    }

    @Override
    public String getCode() {
        return code;
    }

    public float getRealWidth() {
        return realWidth;
    }

    public float getRealHeight() {
        return realHeight;
    }
}
