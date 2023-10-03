package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.manager.OldPageManager;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.presetable.LogUtil;

public class CommandDetector {
    private static final String TAG = "CommandDetector";

    private static OldPageManager oldPageManager;

    Command actionCommand = new ActionCommand();
    Command symbolicCommand = new SymbolicCommand(this);
    Command calligraphy = new Calligraphy();

    private Writer writer;

//    public CommandDetector() {
//
//    }

    public CommandDetector(Writer writer) {
        this.writer = writer;
    }

    public Command recognize(HandWriting handWriting){

        LogUtil.e(TAG, "开始识别");

        actionCommand.setNext(symbolicCommand);
        symbolicCommand.setNext(calligraphy);
        return actionCommand.handle(handWriting);
    }

    public void setActionCommandAvailable(boolean available){
        this.actionCommand.setAvailable(available);
    }

    public void setSymbolicCommandAvailable(boolean available){
        this.symbolicCommand.setAvailable(available);
        LogUtil.e(TAG, "SymbolicCommandAvailable设置为："+available);
    }

    public void setCalligraphyAvailable(boolean available){
        this.calligraphy.setAvailable(available);
        LogUtil.e(TAG, "CalligraphyAvailable设置为："+available);
    }

    public void response(Command command){
        if(writer != null && command != null){
            writer.response(command);
        }
    }

}
