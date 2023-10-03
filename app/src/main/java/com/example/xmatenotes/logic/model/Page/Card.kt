package com.example.xmatenotes.logic.model.Page

import android.graphics.RectF
import android.os.Build
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.model.Role
import com.example.xmatenotes.logic.model.handwriting.SerializableRectF
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting
import com.example.xmatenotes.util.LogUtil
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class Card() : IPage,Serializable {

    companion object {
        private const val TAG = "Card";
        private const val serialVersionUID: Long = 8500637971569149180L
        private const val rootPath = "CardsData";
        private const val DATE_FORMAT = "yyyy-MMdd-hhmmss"
        private var generalCode = 10

        /**
         * 多为表格存储tableId
         */
        public const val cardsTableId = "tblXcJERkVDkcPki"

        /**
         * 生成卡片存储文件夹名
         */
        @JvmStatic
        fun getCardName(code: String, timelong: String): String {
            return TAG+"#"+code+"#"+getFormatTime(timelong.toLong())
        }

        /**
         * 格式化时间戳
         */
        @JvmStatic
        fun getFormatTime(timelong: Long) : String{
            val sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")
            val datetime: Date = Date(timelong)
            return sdf.format(datetime)
        }
    }

    /**
     * 唯一标识符编码
     */
    var code = ""

    /**
     * 前置编码
     */
    var preCode = ""

    /**
     * 后置编码
     */
    var postCode = ""

    var cardContext: CardContext = CardContext( "", "组长", 0F, 0F, 0F, 0F)
    var cardDataLabel: CardDataLabel = CardDataLabel("", "","语文", "一元一次不等式", "整体认知构建", "2", "七", "00", "00", "00", "秋季学期","20230911")
    var cardResource: CardResource = CardResource( "", ArrayList(), ArrayList())

    /**
     * 卡片资源信息存储
     */
    public var qrObject = QRObject(
        p = "09",//卡片
        psx = 2290,
        psy = 1700,
        pn = "1",//卡片编码
        sc = "01",
        gr = "07",
        cl = "03",
        ca = "2100_150",
        au = "011",
        te = "037",
        st = "01",
        gn = "03",
        gl = "6",//组型
        sub = "01",
        data = "0",
        time = "2023-0911-113300",//1
        qx = 60,
        qy = 60,
        ql = 270
    )

    /**
     * 卡片环境信息
     */
    public data class CardContext (
        var device: String,//迭代设备 1
        var role: String,//迭代角色 1
        var showWidth: Float, //UI版面宽度
        var showHeight: Float, //UI版面高度
        var realWidth: Float, //真实物理版面宽度，10-1mm
        var realHeight: Float, //真实物理版面高度，10-1mm
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 884509906286076997L
        }
    }

    /**
     * 卡片数据标签信息存储
     */
    public data class CardDataLabel(
        //顶栏
        var preCode: String,//前置编码
        var postCode: String,//后置编码
        var subjectName: String, //学科名
        var unitName: String,//单元名
        var stage: String,//阶段
        var classTime: String,//课时

        //侧栏
        var week: String,//周
        var group: String,//小组编号
        var classG: String,//班级
        var grade: String,//年级
        var term: String,//学期
        var day: String//日期
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = -846449730274394163L
        }
    }

    /**
     * 卡片资源信息存储
     */
    public data class CardResource(
        var cardStorageName: String,//卡片存储相对路径
        var audioNameList: MutableList<String>,//音频文件名，不含后缀
        var dotList: MutableList<SingleHandWriting>, //笔迹数据
//        var bitmapByteArray: ByteArray?,//图像字节数组
        var left: Float = Float.MAX_VALUE,
        var top: Float = Float.MAX_VALUE,
        var right: Float = Float.MIN_VALUE,
        var bottom: Float = Float.MIN_VALUE,
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = -5994362899161424990L
        }
    }

    /**
     * 添加单次笔迹
     */
    override fun addSingleHandWriting(singleHandWriting: SingleHandWriting): Card{
        this.cardResource.dotList.add(singleHandWriting)
        return this
    }

//    /**
//     * 将Bitmap对象转换为字节数组
//     */
//    fun serializeBitmap(bitmap: Bitmap) {
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        this.cardResource.bitmapByteArray =  stream.toByteArray()
//    }
//
//    /**
//     * 将字节数组反序列化为Bitmap对象
//     */
//    fun deserializeBitmap(byteArray: ByteArray): Bitmap {
//        return BitmapFactory.decodeByteArray(this.cardResource.bitmapByteArray, 0, byteArray.size)
//    }

    init {
        //        cardResource.dotList.add(SingleHandWriting())
    }

    /**
     * 配置版面UI尺寸和真实物理尺寸
     */
    fun setDimensions(showWidth: Float, showHeight: Float, dpi: Float): CoordinateConverter {
        this.cardContext.showWidth = showWidth
        this.cardContext.showHeight = showHeight
        this.cardContext.realWidth = (showWidth / dpi) *254
        this.cardContext.realHeight = (showHeight / dpi) *254
        return createCoordinateConverter()
    }

    fun savePreCode(preCode : String){
        if (preCode.isNotEmpty()){
            this.preCode = preCode
            this.cardDataLabel.preCode = this.preCode
//            this.code = this.preCode.substring(0,4)
//            this.qrObject.pn = this.code
        }
    }

    fun savePostCode(postCode: String){
        if(postCode.isNotEmpty()){
            this.postCode = postCode
            this.cardDataLabel.postCode = this.postCode
        }

    }

    fun setIteration(oldIteration: String){
        this.qrObject.data = (Integer.parseInt(oldIteration)+1).toString()
    }

    /**
     * 设置真实物理版面尺寸
     */
    fun setPageRect(width: Int, height: Int){
        this.qrObject.psx = width
        this.qrObject.psy = height
    }

    fun setQRCodeRect(left: Int, top: Int, long: Int){
        this.qrObject.qx = left
        this.qrObject.qy = top
        this.qrObject.ql = long
    }

    /**
     * 生成坐标转换器
     */
    private fun createCoordinateConverter(): CoordinateConverter {
        return CoordinateConverter(this.cardContext.showWidth, this.cardContext.showHeight, this.cardContext.realWidth,this.cardContext.realHeight)
    }

    /**
     * 添加笔迹点集
     */
    fun addDotList(index: Int, dotList: MutableList<SingleHandWriting>){
        this.cardResource.dotList.addAll(index, dotList)
    }

    fun addAudioNameList(index: Int, audioNameList: MutableList<String>){
        this.cardResource.audioNameList.addAll(index, audioNameList)
    }

    /**
     * 生成一个新音频文件名，不含后缀，并返回
     */
    fun getNewAudioName(): String {
        var newAudioName = this.cardResource.audioNameList.size.toString()
        this.cardResource.audioNameList.add(newAudioName)
        LogUtil.e(TAG, "getNewAudioName: audioNameList.size为: "+this.cardResource.audioNameList.size)
        return newAudioName
    }

    fun getAudioNameList(): MutableList<String> {
        return this.cardResource.audioNameList
    }

    /**
     * 获取最后一个音频文件名
     */
    fun getLastAudioName(): String {
        var lastName = this.getAudioNameList()[getAudioNameList().size-1]
        LogUtil.e(TAG, "getLastAudioName: $lastName")
        return lastName
    }

    /**
     * 根据坐标获取可能叠加的音频，若无音频，返回null
     */
    fun getAudioNameByCoordinate(dot: SimpleDot): String?{
        for (singleHandWriting in this.cardResource.dotList){
            for (handWriting in singleHandWriting.handWritings){
                LogUtil.e(TAG, "handWriting.audioId: "+handWriting.audioId)
                if(handWriting.audioId != XmateNotesApplication.DEFAULT_INT && handWriting.contains(dot)){
                    LogUtil.e(TAG, "坐标上叠加的音频为: "+handWriting.audioId)
                    return handWriting.audioId.toString()
                }
            }
        }
        return null
    }

    /**
     *
     */
    fun getCardName(): String{
        //版面类型+唯一标识符+生成时间
        cardResource.cardStorageName = getCardName(this.code, this.qrObject.time)
        return cardResource.cardStorageName
    }

    /**
     * 确定最终生成时间
     */
    fun create(){

        LogUtil.e(TAG, "原始getHandWritingsRectF():"+getHandWritingsRectF())
        LogUtil.e(TAG, "已有singleHandWriting数量: "+this.cardResource.dotList.size)
        var rectF = SerializableRectF()
        for (singleHandWriting in this.cardResource.dotList){
            if(singleHandWriting.boundRectF.equals(rectF) || !singleHandWriting.isNew){
                //若为空，或旧笔迹，直接跳过
                continue
            }
            LogUtil.e(TAG, "前getHandWritingsRectF():"+getHandWritingsRectF())
            LogUtil.e(TAG, "singleHandWriting.boundRectF: "+singleHandWriting.boundRectF)
            this.cardResource.left = if(this.cardResource.left > singleHandWriting.boundRectF.left){
                singleHandWriting.boundRectF.left
            } else {
                this.cardResource.left
            }
            this.cardResource.top = if(this.cardResource.top > singleHandWriting.boundRectF.top){
                singleHandWriting.boundRectF.top
            } else {
                this.cardResource.top
            }
            this.cardResource.right = if(this.cardResource.right < singleHandWriting.boundRectF.right){
                singleHandWriting.boundRectF.right
            } else {
                this.cardResource.right
            }
            this.cardResource.bottom = if(this.cardResource.bottom < singleHandWriting.boundRectF.bottom){
                singleHandWriting.boundRectF.bottom
            } else {
                this.cardResource.bottom
            }
            LogUtil.e(TAG, "后getHandWritingsRectF():"+getHandWritingsRectF())
        }

        this.qrObject.time = System.currentTimeMillis().toString()
        this.code = this.preCode.substring(0,4) + this.qrObject.time
        this.qrObject.pn = this.code
    }

    /**
     * 获取新增迭代笔迹所在矩形区域
     */
    fun getHandWritingsRectF() : RectF{
        return RectF(cardResource.left, cardResource.top, cardResource.right, cardResource.bottom)
    }

    /**
     * 获取设备信息
     */
    fun getDevice() : String{
        this.cardContext.device =  Build.MODEL
        return this.cardContext.device
    }

    /**
     * 获取角色身份信息
     */
    fun getRole() : String{
        this.cardContext.role = "组长"
        return this.cardContext.role
    }

    /**
     * 获取班级
     */
    fun getClassG() : String{
        this.qrObject.cl = "01"
        this.cardDataLabel.classG = this.qrObject.cl
        return this.qrObject.cl
    }

    /**
     * 获取年级
     */
    fun getGrade() : String{
        this.qrObject.gr = "01"
        this.cardDataLabel.grade = this.qrObject.gr
        return this.qrObject.gr
    }

    /**
     * 获取小组
     */
    fun getGroup() : String{
        this.qrObject.gn = "01"
        this.cardDataLabel.group = this.qrObject.gn
        return this.qrObject.gn
    }

    /**
     * 获取学生编号
     */
    fun getStudentNumber() : String{
        this.qrObject.st = "01"
        return this.qrObject.st
    }

    fun toQRObject() : QRObject{
//        this.qrObject.time = DateUtil.formatTimelong(System.currentTimeMillis(), DATE_FORMAT)
        //初始化卡片存储目录
        this.create()
        return this.qrObject
    }

    fun toMap() : MutableMap<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["Code"] = this.code
        map["preCode"] = this.preCode
        map["postCode"] = this.postCode
        map["数据创建角色"] = this.cardContext.role
        map["学校"] = this.qrObject.sc
        map["年级"] = this.qrObject.gr
        map["班级"] = this.qrObject.cl
        map["学期"] = this.cardDataLabel.term
        map["版面类别"] = this.qrObject.p
        map["教师编号"] = this.qrObject.te
        map["学生编号"] = this.qrObject.st
        map["小组编号"] = this.qrObject.gn
        map["小组组型"] = this.qrObject.gl
        map["学科"] = this.cardDataLabel.subjectName
        map["单元"] = this.cardDataLabel.unitName
        map["Left"] = this.cardResource.left
        map["Top"] = this.cardResource.top
        map["Right"] = this.cardResource.right
        map["Bottom"] = this.cardResource.bottom
        map["卡片名称"] = this.getCardName()
        map["更新时间"] = this.qrObject.time.toLong()
        return map
    }

    /**
     * 获取小组组型
     */
    fun getGroupClass() : String{
        this.qrObject.gl = "06"
        return this.qrObject.gl
    }

    /**
     * 更新角色身份信息
     */
    fun updateRole(role: Role){
        //角色
        this.cardContext.role = role.role
        //学生编号
        this.qrObject.st = role.studentNumber
        //小组编号
        this.cardDataLabel.group = role.groupNumber
        this.qrObject.gn = role.groupNumber
        //小组组型
        this.qrObject.gl = role.groupTip
        //学校
        this.qrObject.sc = role.school
        //班级
        this.cardDataLabel.classG = role.classNumber
        this.qrObject.cl = role.classNumber
        //年级
        this.cardDataLabel.grade = role.grade
        this.qrObject.gr = role.grade
    }

    /**
     * 自动初始化
     */
    fun init(): Card{
        getDevice()
        getRole()
        getStudentNumber()
        getGroupClass()
        return this
    }

}