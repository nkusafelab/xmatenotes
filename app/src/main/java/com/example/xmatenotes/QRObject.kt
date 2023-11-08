package com.example.xmatenotes

data class QRObject(
    var p:String,//版面信息
    var psx:String,//版面尺寸x
    var psy:String,//版面尺寸y
    var pn:String,//版面页码
    var sc:String,//学校编号
    var gr:String,//年级编号
    var cl:String,//班级编号
    var ca:String,//拍摄尺寸
    var au:String,//数据来源
    var te:String,//教师编号
    var st:String,//学生编号
    var gn:String,//小组编号
    var gl:String,//小组组型
    var sub:String,//科目?
    var data:String,//数据?
    var time:String,//时间
    var qx:String,//左上坐标x
    var qy:String,//左上坐标y
    var ql:String//边长
)