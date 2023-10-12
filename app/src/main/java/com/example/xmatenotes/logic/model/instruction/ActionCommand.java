package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.util.LogUtil;

public class ActionCommand extends Command {

    private static final String TAG = "ActionCommand";
    private Command nextCommand;
    private ActionCommand singleClick = null;
    private ActionCommand longPress = null;
    private ActionCommand doubleCLick = null;

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
    public String getTag() {
        return TAG;
    }

    @Override
    public Command handle(HandWriting handWriting) {
        Command command = null;
        if (getID() < 0) {//ActionCommand
            if (isAvailable()) {
                if(this.singleClick == null){
                    this.singleClick = new SingleClick();
                }
                if(this.longPress == null){
                    this.longPress = new LongPress();
                }
                if(this.doubleCLick == null){
                    this.doubleCLick = new DoubleClick();
                }
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
            boolean result = this.recognize(handWriting);
            LogUtil.e(getTag(), getTag()+"识别结果为： "+result);
            if (isAvailable() && result) {
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
