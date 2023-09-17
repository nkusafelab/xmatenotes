package com.example.xmatenotes.logic.model.instruction;

import android.graphics.Color;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

public abstract class Responser implements Observer, CommandResponse {

    private static final String TAG = "Responser";

    @Override
    public void update(Observable o, Object arg) {

        if(o instanceof Command){
            Command com = (Command)o;
            String comName = com.getName();
            Class responserClass = this.getClass();
            try {
                Method method = responserClass.getDeclaredMethod("on"+comName,Command.class);
                method.invoke(this, com);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        //动作命令
        if(o instanceof ActionCommand){
            ActionCommand actionCommand = (ActionCommand)o;
            this.onActionCommand(actionCommand);
        }

        //符号命令
        if(o instanceof  SymbolicCommand){
            SymbolicCommand symbolicCommand = (SymbolicCommand) o;
            this.onSymbolicCommand(symbolicCommand);
        }

    }

    @Override
    public void onActionCommand(Command command) {
        if(!command.getHandWriting().isClosed()){
            return;
        }
        LogUtil.e(TAG, "onActionCommand");
        command.getHandWriting().clear();
    }

    @Override
    public void onSingleClick(Command command) {
        if(!command.getHandWriting().isClosed()){
            return;
        }
        LogUtil.e(TAG, "onSingleClick");

    }

    @Override
    public void onDoubleClick(Command command) {
        if(!command.getHandWriting().isClosed()){
            return;
        }
        LogUtil.e(TAG, "onDoubleClick");
    }

    @Override
    public void onLongPress(Command command) {
        if(!command.getHandWriting().isClosed()){
            return;
        }
        LogUtil.e(TAG, "onLongPress");
    }

    @Override
    public void onCalligraphy(Command command) {
        LogUtil.e(TAG, "onCalligraphy");
    }

    @Override
    public void onSymbolicCommand(Command command) {
        LogUtil.e(TAG, "onSymbolicCommand");
        HandWriting handWriting = command.getHandWriting();
        handWriting.setColor(Color.BLUE);
        handWriting.setWidth(HandWriting.DEFAULT_BOLD_WIDTH);
    }

    @Override
    public void onZhiLingKongZhi(Command command) {
        LogUtil.e(TAG, "onZhiLingKongZhi");
    }

    @Override
    public void onDui(Command command) {
        LogUtil.e(TAG, "onDui");
    }

    @Override
    public void onBanDui(Command command) {
        LogUtil.e(TAG, "onBanDui");
    }

    @Override
    public void onBanBanDui(Command command) {
        LogUtil.e(TAG, "onBanBanDui");
    }

    @Override
    public void onBanBanBanDui(Command command) {
        LogUtil.e(TAG, "onBanBanBanDui");
    }

    @Override
    public void onCha(Command command) {
        LogUtil.e(TAG, "onCha");
    }

    @Override
    public void onWen(Command command) {
        LogUtil.e(TAG, "onWen");
    }

    @Override
    public void onBanWen(Command command) {
        LogUtil.e(TAG, "onBanWen");
    }

    @Override
    public void onBanBanWen(Command command) {
        LogUtil.e(TAG, "onBanBanWen");
    }

    @Override
    public void onBanBanBanWen(Command command) {
        LogUtil.e(TAG, "onBanBanBanWen");
    }

    @Override
    public void onTan(Command command) {
        LogUtil.e(TAG, "onTan");
    }

    @Override
    public void onBanTan(Command command) {
        LogUtil.e(TAG, "onBanTan");
    }

    @Override
    public void onBanBanTan(Command command) {
        LogUtil.e(TAG, "onBanBanTan");
    }

    @Override
    public void onBanBanBanTan(Command command) {
        LogUtil.e(TAG, "onBanBanBanTan");
    }
}
