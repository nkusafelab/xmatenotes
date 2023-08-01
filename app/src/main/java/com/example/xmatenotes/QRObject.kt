package com.example.xmatenotes

data class QRObject(
    val p:String,//版面信息
    val psx:String,//版面尺寸x
    val psy:String,//版面尺寸y
    val pn:String,//版面页码
    val sc:String,//学校编号
    val gr:String,//年级编号
    val cl:String,//班级编号
    val ca:String,//拍摄尺寸
    val au:String,//数据来源
    val te:String,//教师编号
    val st:String,//学生编号
    val gn:String,//小组编号
    val gl:String,//小组组型
    val sub:String,//科目?
    val data:String,//数据?
    val time:String,//时间
    val qx:String,//左上坐标x
    val qy:String,//左上坐标y
    val ql:String//边长
)
