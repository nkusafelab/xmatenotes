package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;

import java.util.Observable;

public abstract class Command extends Observable {

    private static final String TAG = "Command";

    private HandWriting handWriting;

    //识别是否有效
    private boolean available = true;

    public Command(){

    }

    public Command(HandWriting handWriting) {
        this.handWriting = handWriting;
    }

    public abstract int getID();

    public abstract String getName();
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

}
