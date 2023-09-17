package com.example.xmatenotes.logic.model.instruction;

/**
 * 命令响应接口
 */
public interface CommandResponse {

    void onActionCommand(Command command);

    void onSingleClick(Command command);

    void onDoubleClick(Command command);

    void onLongPress(Command command);

    void onCalligraphy(Command command);

    void onSymbolicCommand(Command command);

    void onZhiLingKongZhi(Command command);

    void onDui(Command command);

    void onBanDui(Command command);

    void onBanBanDui(Command command);

    void onBanBanBanDui(Command command);

    void onCha(Command command);

    void onWen(Command command);

    void onBanWen(Command command);

    void onBanBanWen(Command command);

    void onBanBanBanWen(Command command);

    void onTan(Command command);

    void onBanTan(Command command);

    void onBanBanTan(Command command);

    void onBanBanBanTan(Command command);

}
