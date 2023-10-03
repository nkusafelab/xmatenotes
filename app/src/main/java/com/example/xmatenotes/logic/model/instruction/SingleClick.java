package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.Stroke;
import com.example.xmatenotes.logic.presetable.LogUtil;

/**
 * 单击
 */
public class SingleClick extends ActionCommand{

    private static final String TAG = "SingleClick";

    public final static double SINGLE_CLICK_dLIMIT = 10;//定义单击笔划的最大上下或左右距离，10-1mm
    public final static long SINGLE_CLICK_tLIMIT = 300;//定义单击笔划的最大时间跨度
    public final static int STROKES_NUMBER = 1;//定义单击命令的笔划数

    private Command nextCommand;

    public SingleClick(){

    }

    public SingleClick(HandWriting handWriting){
        super(handWriting);
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected boolean recognize(HandWriting handWriting) {

        LogUtil.e(TAG,getTag()+"开始识别");
        if(handWriting.getStrokesNumber() == STROKES_NUMBER){
            return recognize(handWriting.getFirstStroke());
        }
        return false;
    }

    public static boolean recognize(Stroke stroke){
        if(stroke.getDuration() < SINGLE_CLICK_tLIMIT){
            double width = stroke.getBoundRectF().width();
            double height = stroke.getBoundRectF().height();
            LogUtil.e(TAG, "width: "+width+" height: "+height);
            if(width < SINGLE_CLICK_dLIMIT && height < SINGLE_CLICK_dLIMIT){
                return true;
            }
        }
        return false;
    }

    @Override
    protected Command createCommand(HandWriting handWriting) {
        return new SingleClick(handWriting);
    }

}
