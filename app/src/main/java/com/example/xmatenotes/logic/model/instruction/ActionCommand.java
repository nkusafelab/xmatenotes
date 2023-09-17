package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;

public class ActionCommand extends Command {

    private Command nextCommand;
    private ActionCommand singleClick = new SingleClick();
    private ActionCommand longPress = new LongPress();
    private ActionCommand doubleCLick = new DoubleClick();

    public ActionCommand() {
    }

    public ActionCommand(HandWriting handWriting) {
        super(handWriting);
    }

    @Override
    public int getID() {
        return -1;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Command handle(HandWriting handWriting) {
        Command command = null;
        if (getID() < 0) {
            if (isAvailable()) {
                singleClick.setNext(longPress);
                longPress.setNext(doubleCLick);
                doubleCLick.setNext(this.nextCommand);
                return singleClick.handle(handWriting);
            } else {
                if (this.nextCommand != null) {
                    return this.nextCommand.handle(handWriting);
                }
            }
        } else {
            if (isAvailable() && this.recognize(handWriting)) {
                command = this.createCommand(handWriting);
            } else {
                if (this.nextCommand != null) {
                    command = this.nextCommand.handle(handWriting);

                }
            }
            return command;
        }
        return command;
    }


    public void setNext(Command command){
        this.nextCommand = command;
    }

    protected boolean recognize(HandWriting handWriting) {
        return false;
    }

    protected Command createCommand(HandWriting handWriting) {
        return null;
    }

}
