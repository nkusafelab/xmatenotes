package com.example.xmatenotes.logic.manager
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.xmatenotes.logic.dao.RoleDao
//import com.example.xmatenotes.logic.model.Role
//
//@Database(version = 1, entities = [Role::class],exportSchema=false)
//abstract class AppDataBase  : RoomDatabase() {
//
//    abstract fun roleDao() : RoleDao
//
//    companion object{
//        private var instance: AppDataBase? = null
//
//        @Synchronized
//        @JvmStatic
//        fun getDatabase(context: Context): AppDataBase {
//            instance?.let{
//                return it
//            }
//
//            return Room.databaseBuilder(context.applicationContext, AppDataBase::class.java,"app_database").allowMainThreadQueries().build().apply { instance = this }
//        }
//
//    }
//}