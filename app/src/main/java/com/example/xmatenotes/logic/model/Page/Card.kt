package com.example.xmatenotes.logic.model.Page

import android.graphics.RectF
import android.os.Build
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.model.Role
import com.example.xmatenotes.logic.model.handwriting.HandWriting
import com.example.xmatenotes.logic.model.handwriting.SerializableRectF
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting
import com.example.xmatenotes.util.LogUtil
import java.io.Serializable
import java.util.*

class Card: Page(),Serializable {

    companion object {
        private const val TAG = "Card";
        private const val serialVersionUID: Long = 8500637971569149180L
        private const val rootPath = "CardsData";
        private const val DATE_FORMAT = "yyyy-MMdd-hhmmss"
        private var generalCode = 10

    }

    var cardContext: CardContext = CardContext( "", "组长", 0F, 0F, 0F, 0F)
    var cardDataLabel: CardDataLabel = CardDataLabel("", "","语文", "一元一次不等式", "整体认知构建", "2", "七", "00", "00", "00", "秋季学期","20230911")
    var cardResource: CardResource = CardResource( "", ArrayList(), ArrayList())

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

    fun savePreCode(preCode : String){
        if (preCode.isNotEmpty()){
            this.preCode = preCode
            this.cardDataLabel.preCode = this.preCode
            this.code = this.preCode.substring(0,6)
            this.qrObject.pn = this.code
        }
    }

    fun savePostCode(postCode: String){
        if(postCode.isNotEmpty()){
            this.postCode = postCode
            this.cardDataLabel.postCode = this.postCode
        }

    }

    /**
     * 生成坐标转换器
     */
    private fun createCoordinateConverter(): CoordinateConverter {
        return CoordinateConverter(this.cardContext.showWidth, this.cardContext.showHeight, this.cardContext.realWidth,this.cardContext.realHeight)
    }

    /**
     *
     */
    fun getCardName(): String{
        //版面类型+唯一标识符+生成时间
        cardResource.cardStorageName = getPageStorageName(code, qrObject.time)
        return cardResource.cardStorageName
    }

    /**
     * 获取设备信息
     */
    fun getDevice() : String{
        this.cardContext.device =  Build.MODEL
        return this.cardContext.device
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
//
//    fun toMap() : MutableMap<String, Any> {
//        val map: MutableMap<String, Any> = HashMap()
//        map["Code"] = this.code
//        map["preCode"] = this.preCode
//        map["postCode"] = this.postCode
//        map["数据创建角色"] = this.cardContext.role
//        map["学校"] = this.qrObject.sc
//        map["年级"] = this.qrObject.gr
//        map["班级"] = this.qrObject.cl
//        map["学期"] = this.cardDataLabel.term
//        map["版面类别"] = this.qrObject.p
//        map["教师编号"] = this.qrObject.te
//        map["学生编号"] = this.qrObject.st
//        map["小组编号"] = this.qrObject.gn
//        map["小组组型"] = this.qrObject.gl
//        map["学科"] = this.cardDataLabel.subjectName
//        map["单元"] = this.cardDataLabel.unitName
//        map["Left"] = this.cardResource.left
//        map["Top"] = this.cardResource.top
//        map["Right"] = this.cardResource.right
//        map["Bottom"] = this.cardResource.bottom
//        map["卡片名称"] = this.getCardName()
//        map["更新时间"] = this.qrObject.time.toLong()
//        return map
//    }

    /**
     * 获取小组组型
     */
    fun getGroupClass() : String{
        this.qrObject.gl = "06"
        return this.qrObject.gl
    }

//    /**
//     * 更新角色身份信息
//     */
//    fun updateRole(role: Role){
//        //角色
//        this.cardContext.role = role.roleName
//        //学生编号
//        this.qrObject.st = role.studentNumber
//        //小组编号
//        this.cardDataLabel.group = role.groupNumber
//        this.qrObject.gn = role.groupNumber
//        //小组组型
//        this.qrObject.gl = role.groupTip
//        //学校
//        this.qrObject.sc = role.school
//        //班级
//        this.cardDataLabel.classG = role.classNumber
//        this.qrObject.cl = role.classNumber
//        //年级
//        this.cardDataLabel.grade = role.grade
//        this.qrObject.gr = role.grade
//    }

    /**
     * 自动初始化
     */
    fun init(): Card{
        getDevice()
        getStudentNumber()
        getGroupClass()
        return this
    }

}