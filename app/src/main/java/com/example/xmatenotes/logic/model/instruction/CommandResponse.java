package com.example.xmatenotes.logic.model.instruction;

/**
 * 命令响应接口
 */
public interface CommandResponse {

    boolean onActionCommand(Command command);

    boolean onSingleClick(Command command);

    boolean onDoubleClick(Command command);

    boolean onLongPress(Command command);

    boolean onCalligraphy(Command command);

    boolean onDelayHandWriting(Command command);

    boolean onDelaySingleHandWriting(Command command);

    boolean onSymbolicCommand(Command command);

    boolean onZhiLingKongZhi(Command command);

    boolean onDui(Command command);

    boolean onBanDui(Command command);

    boolean onBanBanDui(Command command);

    boolean onBanBanBanDui(Command command);

    boolean onCha(Command command);

    boolean onWen(Command command);

    boolean onBanWen(Command command);

    boolean onBanBanWen(Command command);

    boolean onBanBanBanWen(Command command);

    boolean onTan(Command command);

    boolean onBanTan(Command command);

    boolean onBanBanTan(Command command);

    boolean onBanBanBanTan(Command command);

}
