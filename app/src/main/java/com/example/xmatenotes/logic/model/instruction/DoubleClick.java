package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.Stroke;

/**
 * <p><strong>双击</strong></p>
 */
public class DoubleClick extends ActionCommand{

    private static final String TAG = "DoubleClick";

    //时间常量
    public final static int DOUBLE_CLICK_PERIOD = 300;//定义双击的两次点击最大时间间隔

    public final static int STROKES_NUMBER = 2;//定义单击命令的笔划数

    private Command nextCommand;

    public DoubleClick(){

    }

    public DoubleClick(HandWriting handWriting){
        super(handWriting);
    }

    @Override
    public int getID() {
        return 2;
    }

    @Override
    public String getName() {
        return "DoubleClick";
    }

    @Override
    protected boolean recognize(HandWriting handWriting) {
        if(handWriting.getStrokesNumber() == STROKES_NUMBER){
            for (Stroke stroke: handWriting.getStrokes()) {
                if(!SingleClick.recognize(stroke)){
                    return false;
                }
            }
            if(handWriting.getStrokes().get(STROKES_NUMBER-1).getPrePeriod() >= DOUBLE_CLICK_PERIOD){
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected Command createCommand(HandWriting handWriting) {
        return new DoubleClick(handWriting);
    }
}
