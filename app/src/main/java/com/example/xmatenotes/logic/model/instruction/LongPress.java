package com.example.xmatenotes.logic.model.instruction;

import static com.example.xmatenotes.logic.model.instruction.SingleClick.SINGLE_CLICK_dLIMIT;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.Stroke;
import com.example.xmatenotes.logic.model.instruction.Instruction;

/**
 * <p><strong>长压</strong></p>
 */
public class LongPress extends ActionCommand {

    private static final String TAG = "LongPress";

    public final static long LONG_PRESS_tLIMIT = 2000;//定义长压笔划的最小时间跨度
    public final static int STROKES_NUMBER = 1;//定义单击命令的笔划数

    private Command nextCommand;

    public LongPress(){

    }

    public LongPress(HandWriting handWriting){
        super(handWriting);
    }

    @Override
    public int getID() {
        return 3;
    }

    @Override
    public String getName() {
        return "LongPress";
    }

    @Override
    protected boolean recognize(HandWriting handWriting) {
        if(handWriting.getStrokesNumber() == STROKES_NUMBER){
            return recognize(handWriting.getFirstStroke());
        }
        return false;
    }

    public static boolean recognize(Stroke stroke){
        if(stroke.getDuration() > LONG_PRESS_tLIMIT){
            double width = stroke.getBoundRectF().width();
            double height = stroke.getBoundRectF().height();
            if(width < SINGLE_CLICK_dLIMIT && height < SINGLE_CLICK_dLIMIT){
                return true;
            }
        }
        return false;
    }

    @Override
    protected Command createCommand(HandWriting handWriting) {
        return new LongPress(handWriting);
    }
}
