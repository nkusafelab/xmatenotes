package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.manager.PageManager;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;

public class CommandDetector {
    private static final String TAG = "CommandDetector";

    private static PageManager pageManager;

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

        actionCommand.setNext(symbolicCommand);
        symbolicCommand.setNext(calligraphy);
        return actionCommand.handle(handWriting);
    }

    public void setActionCommandAvailable(boolean available){
        this.actionCommand.setAvailable(available);
    }

    public void setSymbolicCommandAvailable(boolean available){
        this.symbolicCommand.setAvailable(available);
    }

    public void setCalligraphyAvailable(boolean available){
        this.calligraphy.setAvailable(available);
    }

    public void response(Command command){
        if(writer != null){
            writer.response(command);
        }
    }

}
