package com.example.xmatenotes.logic.model;

public class CardData {
    private String title;
    private String content;

    private String time;

    public CardData(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTime(){
        return time;
    }


}
