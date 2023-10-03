package com.example.xmatenotes.logic.model.instruction;

import androidx.annotation.NonNull;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.presetable.LogUtil;

import java.util.Observable;

public abstract class Command extends Observable implements Cloneable {

    private static final String TAG = "Command";

    private HandWriting handWriting;

    //识别是否有效
    private boolean available = true;

    private String name;

    public Command(){

    }

    public Command(HandWriting handWriting) {
        this.handWriting = handWriting;
        setName(getTag());
    }

    public abstract int getID();

    public String getName(){
        if(this.name != null){
            return this.name;
        }

        LogUtil.e(TAG, "getName(): this.name为null");
        return null;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getTag(){
        return TAG;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public abstract Command handle(HandWriting handWriting);

    public abstract void setNext(Command command);

    public HandWriting getHandWriting(){
        return this.handWriting;
    }

    public void setHandWriting(HandWriting handWriting) {
        this.handWriting = handWriting;
    }

    public void response(){
        super.setChanged();
        super.notifyObservers(getName());
    }

    @NonNull
    @Override
    public Command clone() {
        Command command = null;
        try {
            command = (Command)super.clone();
            command.setHandWriting(this.handWriting.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return command;
    }
}
