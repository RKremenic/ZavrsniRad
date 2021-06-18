package com.example.zavrsnirad2.models;

public class CardModel {
    private int cardID;
    private String question;
    private String answer;
    private int categoryID;
    private int correctAnswers;
    private float easinessFactor;
    private int repetitionInterval;
    private String nextRepetitionDate;

    public CardModel(int cardID, String question, String answer, int categoryID, int correctAnswers, float easinessFactor, int repetitionInterval, String nextRepetitionDate) {
        this.cardID = cardID;
        this.question = question;
        this.answer = answer;
        this.categoryID = categoryID;
        this.correctAnswers = correctAnswers;
        this.easinessFactor = easinessFactor;
        this.repetitionInterval = repetitionInterval;
        this.nextRepetitionDate = nextRepetitionDate;
    }

    public int getCardID() {
        return cardID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public float getEasinessFactor() {
        return easinessFactor;
    }

    public void setEasinessFactor(float easinessFactor) {
        this.easinessFactor = easinessFactor;
    }

    public int getRepetitionInterval() {
        return repetitionInterval;
    }

    public void setRepetitionInterval(int repetitionInterval) {
        this.repetitionInterval = repetitionInterval;
    }

    public String getNextRepetitionDate() {
        return nextRepetitionDate;
    }

    public void setNextRepetitionDate(String nextRepetitionDate) {
        this.nextRepetitionDate = nextRepetitionDate;
    }
}
