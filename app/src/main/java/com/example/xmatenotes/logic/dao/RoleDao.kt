package com.example.xmatenotes.logic.dao

import android.util.Log
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.Storager
import com.example.xmatenotes.logic.model.Role
import com.example.xmatenotes.util.LogUtil
import com.google.gson.Gson

//
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Update
//import com.example.xmatenotes.logic.model.Role

object RoleDao {

    private const val TAG = "RoleDao"
    const val peopleSharedPreferences = "people"
    private val storager = Storager.getInstance()
    private var role: Role? = null

    fun saveRole(roleName: String, studentNumber: String, groupNumber:String, groupTip:String, school:String, classNumber:String, grade:String, macaddres:String){
        saveRole(Role(roleName, studentNumber, groupNumber, groupTip, school, classNumber, grade, macaddres))
    }
    fun saveRole(role: Role){
        if(XmateNotesApplication.isMacEffective()){
            this.role = role
            val roleString = Gson().toJson(role)
            var mac = role.macaddres
            storager.saveStringBySharedPreferences(mac, roleString)
            LogUtil.e(TAG, "saveRole: 存入mac地址和角色信息: $mac:$roleString")
        } else{
            LogUtil.e(TAG, "saveRole: 蓝牙mac地址无效，存储失败！")
        }
    }

    fun getRole():Role?{
        if(isSavedRole()){
            return this.role!!
        } else {
            if (XmateNotesApplication.isMacEffective()){
                var mac = XmateNotesApplication.mBTMac
                return getRole(mac)
            }
            LogUtil.e(TAG, "getRole: 未连接点阵笔蓝牙！")
            return null
        }
    }

    fun getRole(mac:String):Role?{
        var roleString = storager.getStringBySharedPreferences(mac)
        if(roleString != null){
            this.role = Gson().fromJson(roleString, Role::class.java)
            return this.role
        } else {
            LogUtil.e(TAG, "getRole: 获取角色信息失败！")
            return null
        }
    }

    fun isSavedRole() = this.role != null

}



//
//@Dao
//interface RoleDao {
//
//    @Insert
//    fun insertRole(role: Role) : Long  //插入对象。并返回主键,也就是mac地址
//
//    @Update
//    fun updateRole(newRole: Role)   //更新对象
//
//    @Query( "select * from Role")
//    fun loadAllRole():List<Role>   //加载所有对象
//
//    @Delete
//    fun deleteBindMessageRole(role: Role)  //删除某一个对象
//
//    @Query("select * from Role where macaddress = :macaddress")
//    fun loadRoleByMacAddress(macaddress : String) : Role
//}
