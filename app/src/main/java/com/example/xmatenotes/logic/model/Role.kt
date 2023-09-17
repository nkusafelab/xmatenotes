package com.example.xmatenotes.logic.model

import java.io.Serializable

//import androidx.room.Entity
//import androidx.room.PrimaryKey

//@Entity
data class Role(var role:String,var studentNumber :String ,var groupNumber:String ,var groupTip:String ,var school:String ,var classNumber:String ,var grade:String,var macaddres:String):
    Serializable {
//    @PrimaryKey(autoGenerate = true)
//    var id : Long =   0//在上述的所有的元素之后又加入了一个macaddress作为主键

}