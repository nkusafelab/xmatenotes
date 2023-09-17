package com.example.xmatenotes.logic.dao
//
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Update
//import com.example.xmatenotes.logic.model.Role
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