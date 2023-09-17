package com.example.xmatenotes.logic.model.Page

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Build
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class Card() : Serializable {

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
    }

    public var subjectToColorMap:Map<String,Int> = mapOf("数学" to 0x7F82BB,"语文" to 0xB5E61D,"英语" to 0x9FFCFD,
        "物理" to 0xEF88BE,"化学" to 0xFFFD55,"生物" to 0x58135E,"政治" to 0x16417C)


    /**
     * 唯一标识符编码
     */
    public var code = 2

    /**
     * 前置编码
     */
    public var preCode = 1

    /**
     * 后置编码
     */
    public var postCode = 0

    public lateinit var cardContext: CardContext
    public lateinit var cardDataLabel: CardDataLabel
    public lateinit var cardResource: CardResource
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
        st = "01GM",
        gn = "03",
        gl = "6",
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
        var preCode: String,
        var subjectName: String, //1
        var unitName: String,
        var stage: String,
        var classTime: String,

        //侧栏
        var week: String,
        var group: String,
        var classG: String,
        var grade: String,
        var term: String,
        var day: String
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = -846449730274394163L
        }
    }

    /**
     * 卡片资源信息存储
     */
    public data class CardResource(
        var cardStorageName: String,
        var audioNameList: MutableList<String>,
        var dotList: MutableList<SingleHandWriting>, //笔迹数据
//        var bitmapByteArray: ByteArray?,//图像字节数组
        var left: Float = 0F,
        var top: Float = 0F,
        var right: Float = 0F,
        var bottom: Float = 0F,
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = -5994362899161424990L
        }
    }

    /**
     * 添加单次笔迹
     */
    fun addSingleHandWriting(singleHandWriting: SingleHandWriting){
        this.cardResource.dotList.add(singleHandWriting)
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
        cardContext = CardContext( "", "组长", 0F, 0F, 0F, 0F)
        cardDataLabel = CardDataLabel("", "", "一元一次不等式", "整体认知构建", "2", "", "", "", "", "", "")
        cardResource = CardResource( "", ArrayList(), ArrayList())
        cardResource.dotList.add(SingleHandWriting())
    }

    /**
     * 配置版面UI尺寸和真实物理尺寸
     */
    fun setDimensions(showWidth: Float, showHeight: Float, dpi: Float){
        this.cardContext.showWidth = showWidth
        this.cardContext.showHeight = showHeight
        this.cardContext.realWidth = (showWidth / dpi) *254
        this.cardContext.realHeight = (showHeight / dpi) *254
    }

    fun setPreCode(preCode : String){
        this.preCode = Integer.parseInt(preCode)
        this.cardDataLabel.preCode = this.preCode.toString()
        this.code = this.preCode+1
        this.qrObject.pn = this.code.toString()
    }

    fun setIteration(oldIteration: String){
        this.qrObject.data = (Integer.parseInt(oldIteration)+1).toString()
    }

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
     *
     */
    fun getCardName(): String{
        //版面类型+唯一标识符+生成时间
        cardResource.cardStorageName = TAG+"#"+code.toString()+"#"+getFormatTime(this.qrObject.time.toLong())
        return cardResource.cardStorageName
    }

    /**
     * 确定最终生成时间
     */
    fun create(){
        this.qrObject.time = System.currentTimeMillis().toString()
    }

    fun getFormatTime(timelong: Long) : String{
        val sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")
        val datetime: Date = Date(timelong)
        return sdf.format(datetime)
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
        map["更新时间"] = System.currentTimeMillis()
        return map
    }

    /**
     * 获取小组组型
     */
    fun getGroupClass() : String{
        this.qrObject.gl = "萝卜+"
        return this.qrObject.gl
    }

    /**
     * 自动初始化
     */
    fun init(){
        getDevice()
        getRole()
        getStudentNumber()
        getGroupClass()

    }

}