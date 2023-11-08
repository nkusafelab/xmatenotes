package com.example.xmatenotes.logic.model

import java.io.Serializable

//import androidx.room.Entity
//import androidx.room.PrimaryKey

//@Entity
data class Role(
    /**
     * 角色
     */
    var roleName:String,
    /**
     * 学生编号
     */
    var studentNumber :String,
    /**
     * 小组编号
     */
    var groupNumber:String,
    /**
     * 小组组型
     */
    var groupTip:String,
    /**
     * 学校编号
     */
    var school:String,
    /**
     * 班级
     */
    var classNumber:String,
    /**
     * 年级
     */
    var grade:String,
    /**
     * mac地址
     */
    var macaddres:String):
    Serializable {
//    @PrimaryKey(autoGenerate = true)
//    var id : Long =   0//在上述的所有的元素之后又加入了一个macaddress作为主键
    companion object {

    private const val serialVersionUID: Long = 9022698440141756879L
}

}