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
        LogUtil.e(TAG, "响应开始");

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

        if(o instanceof Command){
            Command com = (Command)o;
            String comName = com.getName();
            Class responserClass = this.getClass();
            try {
                LogUtil.e(TAG, "执行on"+comName+"()");
                Method method = responserClass.getDeclaredMethod("on"+comName,Command.class);
                method.invoke(this, com);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                try {
                    Method method = Responser.class.getDeclaredMethod("on"+comName, Command.class);
                    method.invoke(this, com);
                } catch (NoSuchMethodException | InvocationTargetException |
                         IllegalAccessException ex) {
                    //当您在代码中使用反射调用方法时，如果被调用的方法本身抛出了异常，那么反射调用将会捕获并封装该异常，并抛出InvocationTargetException。
                    throw new RuntimeException(ex);
                }

            }
            if(com.getHandWriting().isClosed() && !isLongPressExecute){
                isLongPressExecute = true;
            }
        }

    }

    @Override
    public boolean onActionCommand(Command command) {
        if(!command.getHandWriting().isClosed()){
            return false;
        }

        LogUtil.e(TAG, "onActionCommand");
        //动作命令不画出来
        HandWriting handWriting = command.getHandWriting().clone();
        command.getHandWriting().clear();//确保动作命令所在的HandWriting是完全独立的，否则可能会误删前面的笔迹
        LogUtil.e(TAG, "onActionCommand: handWritingBuffer清空: command.getHandWriting().clear()");
        command.setHandWriting(handWriting);
        return true;
    }

    @Override
    public boolean onSingleClick(Command command) {
        if(!command.getHandWriting().isClosed()){
            return false;//后续不建议执行
        }
        LogUtil.e(TAG, "onSingleClick");
        return true;
    }

    @Override
    public boolean onDoubleClick(Command command) {
        if(!command.getHandWriting().isClosed()){
            return false;
        }
        LogUtil.e(TAG, "onDoubleClick");
        return true;
    }

    private boolean isLongPressExecute = true;

    @Override
    public boolean onLongPress(Command command) {
        if(isLongPressExecute){
            isLongPressExecute = false;
            return true;
        }
        LogUtil.e(TAG, "onLongPress");
        return false;
    }

    @Override
    public boolean onCalligraphy(Command command) {
        LogUtil.e(TAG, "onCalligraphy");
        return true;
    }

    @Override
    public boolean onDelayHandWriting(Command command) {
        LogUtil.e(TAG, "onDelayHandWriting");
        return true;
    }

    @Override
    public boolean onDelaySingleHandWriting(Command command) {
        LogUtil.e(TAG, "onDelaySingleHandWriting");
        return true;
    }

    @Override
    public boolean onSymbolicCommand(Command command) {
        LogUtil.e(TAG, "onSymbolicCommand");
        HandWriting handWriting = command.getHandWriting();
        handWriting.setColor(Color.BLUE);
        handWriting.setWidth(HandWriting.DEFAULT_BOLD_WIDTH);
        return true;
    }

    @Override
    public boolean onZhiLingKongZhi(Command command) {
        LogUtil.e(TAG, "onZhiLingKongZhi");
        return true;
    }

    @Override
    public boolean onDui(Command command) {
        LogUtil.e(TAG, "onDui");
        return true;
    }

    @Override
    public boolean onBanDui(Command command) {
        LogUtil.e(TAG, "onBanDui");
        return true;
    }

    @Override
    public boolean onBanBanDui(Command command) {
        LogUtil.e(TAG, "onBanBanDui");
        return true;
    }

    @Override
    public boolean onBanBanBanDui(Command command) {
        LogUtil.e(TAG, "onBanBanBanDui");
        return true;
    }

    @Override
    public boolean onCha(Command command) {
        LogUtil.e(TAG, "onCha");
        return true;
    }

    @Override
    public boolean onWen(Command command) {
        LogUtil.e(TAG, "onWen");
        return true;
    }

    @Override
    public boolean onBanWen(Command command) {
        LogUtil.e(TAG, "onBanWen");
        return true;
    }

    @Override
    public boolean onBanBanWen(Command command) {
        LogUtil.e(TAG, "onBanBanWen");
        return true;
    }

    @Override
    public boolean onBanBanBanWen(Command command) {
        LogUtil.e(TAG, "onBanBanBanWen");
        return true;
    }

    @Override
    public boolean onTan(Command command) {
        LogUtil.e(TAG, "onTan");
        return true;
    }

    @Override
    public boolean onBanTan(Command command) {
        LogUtil.e(TAG, "onBanTan");
        return true;
    }

    @Override
    public boolean onBanBanTan(Command command) {
        LogUtil.e(TAG, "onBanBanTan");
        return true;
    }

    @Override
    public boolean onBanBanBanTan(Command command) {
        LogUtil.e(TAG, "onBanBanBanTan");
        return true;
    }
}
