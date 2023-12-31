package com.example.xmatenotes.logic.manager;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.example.xmatenotes.app.XmateNotesApplication;
//import com.example.xmatenotes.logic.dao.RoleDao;
import com.example.xmatenotes.logic.dao.RoleDao;
import com.example.xmatenotes.logic.model.Role;
import com.example.xmatenotes.util.LogUtil;
import com.google.gson.Gson;

public class RoleAManager {
    private static final String TAG = "RoleManager";

//    public static RoleDao roleDao = AppDataBase.getDatabase(XmateNotesApplication.context).roleDao();
    private static final RoleAManager ROLE_A_MANAGER = new RoleAManager();

    private static Role role = null;

    private RoleAManager(){

    }

    public static RoleAManager getInstance(){
        return ROLE_A_MANAGER;
    }

    public static Role getRole(String mac){  //根据MAC地址去数据库中查询
        SharedPreferences pref = XmateNotesApplication.context.getSharedPreferences(RoleDao.peopleSharedPreferences, MODE_PRIVATE);
        String roleString = pref.getString(mac, null);
        LogUtil.e(TAG, "mac: "+mac);
        LogUtil.e(TAG, "getRole: roleString != null: "+(roleString != null));
        if(roleString != null){
            return new Gson().fromJson(roleString, Role.class);
        }
        return null;
//        if(roleDao.loadRoleByMacAddress(XmateNotesApplication.mBTMac) != null){
//
//            return roleDao.loadRoleByMacAddress(XmateNotesApplication.mBTMac);
//        }
//        else{
//            return null;
//        }

    }

//    public static Role getRole(){    //未连接蓝牙时候，去本地查询
//        if(role != null){
//            return role;
//        } else {
//
//        }
//    }

    public static void saveRole(Role role){  //点击保存按钮时，把整个对象插入数据
        String roleString = new Gson().toJson(role);
        SharedPreferences.Editor editor = XmateNotesApplication.context.getSharedPreferences(RoleDao.peopleSharedPreferences, MODE_PRIVATE).edit();
        LogUtil.e(TAG, "saveRole: XmateNotesApplication.mBTMac: "+XmateNotesApplication.mBTMac);
        editor.putString(XmateNotesApplication.mBTMac.toUpperCase(), roleString);
        editor.apply();
//        if (roleDao.loadRoleByMacAddress(XmateNotesApplication.mBTMac) == null){  //如果说是新的绑定
//            roleDao.insertRole(XmateNotesApplication.role);
//
//        }
//        else{
//            roleDao.updateRole(XmateNotesApplication.role);
//
//        }
    }

    public static Role createRole(String role, String studentNumber, String groupNumber, String groupTip, String school, String classNumber, String grade, String macaddres){
        return new Role(role, studentNumber, groupNumber, groupTip, school, classNumber, grade, macaddres);
    }

}
