package com.example.xmatenotes.logic.model;

import java.io.Serializable;
import java.util.List;

public class DataList implements Serializable {
    public List<CardData> CardDataList;

    public DataList(List<CardData> CardDataList){
        this.CardDataList = CardDataList;
    }

    public List<CardData> getCardDataList(){
        return CardDataList;
    }

}
