package com.example.bg.poem;

import lombok.Data;

@Data
public class Poem {
    public String title,poet,text,category,background,appreciation,translation;
    public int PID;
    public int PoID;


    // Getter methods
    public String getTitle() {
        return title;
    }

    public String getPoet() {
        return poet;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public String getBackground() {
        return background;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public String getTranslation() {
        return translation;
    }

    public int getPID() {
        return PID;
    }

    public int getPoID() {
        return PoID;
    }

    // Setter methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setPoet(String poet) {
        this.poet = poet;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public void setPoID(int PoID) {
        this.PoID = PoID;
    }
}
