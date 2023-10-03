package com.example.xmatenotes.logic.model.Page

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.xmatenotes.logic.presetable.LogUtil
import com.example.xmatenotes.util.QRCodeUtil
import com.google.gson.Gson
import java.io.Serializable

data class QRObject(

    //微版面预置信息
    /**
     * 版面类别
     */
    var p:String,
    /**
     * 版面宽度
     */
    var psx:Int,

    /**
     * 版面高度
     */
    var psy:Int,

    /**
     * 版面页码
     */
    var pn:String,

    /**
     * 左上坐标x，不含白边
     */
    var qx:Int,

    /**
     * 左上坐标y，不含白边
     */
    var qy:Int,

    /**
     * 边长，不含白边
     */
    var ql:Int,

    //组织预置信息
    /**
     * 学校编号
     */
    var sc:String,

    /**
     * 年级编号
     */
    var gr:String,

    /**
     * 班级编号
     */
    var cl:String,

    //装备导入信息
    /**
     * 拍摄尺寸

     */
    var ca:String,

    //无感采集生成信息
    /**
     * 数据来源
     */
    var au:String,

    /**
     * 教师编号
     */
    var te:String,

    /**
     * 学生编号
     */
    var st:String,

    /**
     * 小组编号
     */
    var gn:String,

    /**
     * 小组组型
     */
    var gl:String,

    /**
     * 科目?
     */
    var sub:String,

    /**
     * 数据?,迭代次数"0"~"6"
     */
    var data:String,

    /**
     * 生成时间
     */
    var time:String

) : Serializable {

    companion object {
        private const val TAG = "QRObject"
    }

    /**
     * 默认按照字母顺序排列字段
     */
    fun toJosn(): String {
        return Gson().toJson(this)
    }



    /**
     * 将QRObject转换为二维码图片
     * rectF为希望不含白边部分的二维码所占的矩形区域坐标范围，会自动按比例放大，使得最终生成的图片中含白边的二维码所占的矩形区域坐标范围为放大后的rectF
     */
    fun toQRCodeBitmap(rectF: RectF): Bitmap? {
        val text = toJosn()
        LogUtil.e(TAG, "生成Json格式：$text");
//        LogUtil.e(TAG, "反向解析："+Gson().fromJson(text, QRObject::class.java).toString())
//        QRCodeUtil.generateQRCodeRectF(text, rectF.width().toInt(), rectF.height().toInt())
//            ?.let { QRCodeUtil.amplify(it, rectF) }
        return QRCodeUtil.generateQRCodeBitmap(text, rectF, Integer.parseInt(this.data))
    }

    override fun toString(): String {
        return "QRObject(p='$p', psx=$psx, psy=$psy, pn='$pn', qx=$qx, qy=$qy, ql=$ql, sc='$sc', gr='$gr', cl='$cl', ca='$ca', au='$au', te='$te', st='$st', gn='$gn', gl='$gl', sub='$sub', data='$data', time='$time')"
    }
}