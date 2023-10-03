package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;

/**
 * 普通书写
 */
public class Calligraphy extends Command {

    private static final String TAG = "Calligraphy";

    public Calligraphy() {
    }

    public Calligraphy(HandWriting handWriting) {
        super(handWriting);
    }

    @Override
    public int getID() {
        return 18;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Command handle(HandWriting handWriting) {
        return this.createCommand(handWriting);
    }

    @Override
    public void setNext(Command command) {

    }

    protected Command createCommand(HandWriting handWriting) {
        return new Calligraphy(handWriting);
    }
}
