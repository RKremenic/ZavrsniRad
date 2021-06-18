package com.example.zavrsnirad2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.zavrsnirad2.models.CardModel;
import com.example.zavrsnirad2.models.CategoryModel;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DBHelper extends SQLiteOpenHelper {


    public static final String CARD = "kartica";
    public static final String CARD_ID = "sifra_kartice";
    public static final String QUESTION = "pitanje";
    public static final String ANSWER = "odgovor";
    public static final String CATEGORY = "tema";
    public static final String CATEGORY_ID = "sifra_teme";
    public static final String CATEGORY_NAME = "naziv_teme";
    public static final String CORRECT_ANSWERS = "broj_tocnih";
    public static final String EASINESS_FACTOR = "faktor_jednostavnosti";
    public static final String REPETITION_INTERVAL = "interval_ponavljanja";
    public static final String REPETITION_DATE = "datum_ponavljanja";

    public DBHelper(@Nullable Context context) {
        super(context, "zavrsni.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCardTable = "CREATE TABLE " + CARD + " (" + CARD_ID + " INTEGER PRIMARY KEY, "
                + QUESTION + " TEXT, " + ANSWER + " TEXT, " + CATEGORY_ID + " INTEGER, "
                + CORRECT_ANSWERS + " INTEGER, " + EASINESS_FACTOR + " REAL, " + REPETITION_INTERVAL
                + " INTEGER, " + REPETITION_DATE
                + " TEXT, FOREIGN KEY(" + CATEGORY_ID + ") REFERENCES " + CATEGORY +"(" + CATEGORY_ID + "));";
        String createCategoryTable = "CREATE TABLE " + CATEGORY + " (" + CATEGORY_ID + " INTEGER PRIMARY KEY, "
                + CATEGORY_NAME + " TEXT);";

        db.execSQL(createCardTable);
        db.execSQL(createCategoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addCategory(CategoryModel categoryModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY_ID, categoryModel.getCategoryId());
        cv.put(CATEGORY_NAME, categoryModel.getCategoryName());
        long insert = db.insert(CATEGORY, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }
    public boolean addCards(CardModel cardModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CARD_ID, cardModel.getCardID());
        cv.put(QUESTION, cardModel.getQuestion());
        cv.put(ANSWER, cardModel.getAnswer());
        cv.put(CATEGORY_ID, cardModel.getCategoryID());
        if (cardModel.getCorrectAnswers() == -1) {
            cv.putNull(CORRECT_ANSWERS);
            cv.putNull(EASINESS_FACTOR);
            cv.putNull(REPETITION_INTERVAL);
            cv.putNull(REPETITION_DATE);
        } else {
            cv.put(CORRECT_ANSWERS, cardModel.getCorrectAnswers());
            cv.put(EASINESS_FACTOR, cardModel.getEasinessFactor());
            cv.put(REPETITION_INTERVAL, cardModel.getRepetitionInterval());
            cv.put(REPETITION_DATE, cardModel.getNextRepetitionDate());
        }
        long insert = db.insert(CARD, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public String getCategoryNames(boolean downloaded){
        String returnList = "";
        String queryString;
        if (downloaded) {
            queryString = "SELECT DISTINCT " + CATEGORY_NAME + " FROM " + CARD + " NATURAL JOIN " + CATEGORY;
        } else {
            queryString = "SELECT " + CATEGORY_NAME + " FROM " + CATEGORY;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(0);
                returnList += categoryName + "#";
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (returnList.length() > 0) {
            returnList = returnList.substring(0, returnList.length() - 1);
        } else {
            returnList = "Niste preuzeli nijednu temu za učenje";
        }

        return returnList;
    }

    public List<Integer> getCategoryIDs() {
        List<Integer> returnList = new ArrayList<>();
        String queryString = "SELECT * FROM " + CATEGORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.moveToFirst()) {
            do {
                Integer categoryID = cursor.getInt(0);
                returnList.add(categoryID);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return returnList;
    }

    public int getCategoryID(String category) {
        String queryString = "SELECT * FROM " + CATEGORY + " WHERE " + CATEGORY_NAME + " = ?";
        String[] params = new String[] { category };
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        int category_id = cursor.getInt(0);
        cursor.close();
        return category_id;
    }
    public int getCardID(String question) {
        String subString = question.substring(0, question.length() - 4);
        String queryString = "SELECT * FROM " + CARD + " WHERE " + QUESTION + " LIKE \""+subString+"%\"";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { subString };
        Cursor cursor = db.rawQuery(queryString, null);
        cursor.moveToFirst();
        int card_id = cursor.getInt(0);
        cursor.close();
        return card_id;
    }

    public String getCardData(int cardID) {
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CARD_ID + " = ?" ;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(cardID) };
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        String cardData = cursor.getString(1) + "#" + cursor.getString(2) + "#" + cursor.getString(3);
        cursor.close();
        return cardData;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getIntervalAndCorrect(int cardID) {
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CARD_ID + " = ?" ;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(cardID) };
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        int checkNull = cursor.getInt(4);
        String cardData;
        if (Objects.isNull(checkNull)) {
            cardData = "0#0";
        } else {
            cardData = cursor.getInt(4) + "#" + cursor.getInt(6);
        }
        cursor.close();
        return cardData;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public float getCardPoints(int cardID) {
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CARD_ID + " = ?" ;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(cardID) };
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        int checkNull = cursor.getInt(4);
        float points;
        if (Objects.isNull(checkNull)) {
            points = -1;
        } else {
            points = cursor.getFloat(5);
        }
        cursor.close();
        return points;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public float getCategoryPoints(int categoryID) {
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CATEGORY_ID + " = ?" ;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(categoryID) };
        Cursor cursor = db.rawQuery(queryString, params);
        float categoryPoints = 0;
        if (cursor.moveToFirst()) {
            do {
                int checkNull = cursor.getInt(4);
                if (Objects.isNull(checkNull)) {
                    continue;
                } else {
                    categoryPoints += cursor.getFloat(5);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryPoints;
    }

    public List<String> getCardQuestions(int category) {
        List<String> CardQuestions = new ArrayList<>();
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CATEGORY_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { String.valueOf(category) });
        if (cursor.moveToFirst()) {
            do {
                String card = cursor.getString(1);
                if (card.length() > 30) {
                    card = String.format("%.40s", card) + "...";
                }

                CardQuestions.add(card);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return CardQuestions;
    }


    public int getCardsCategoryID(int cardID) {
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CARD_ID + " = ?";
        String IDString = String.valueOf(cardID);
        String[] params = new String[] { IDString };
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        int category_id = cursor.getInt(3);
        cursor.close();
        return category_id;
    }

    public String getCategoryName(int categoryID) {
        String queryString = "SELECT * FROM " + CATEGORY + " WHERE " + CATEGORY_ID + " = ?" ;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(categoryID) };
        Cursor cursor = db.rawQuery(queryString, params);
        cursor.moveToFirst();
        String categoryName = cursor.getString(1);
        cursor.close();
        return categoryName;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getStudyCard(int category_id){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String dateString = formatter.format(date);
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CATEGORY_ID + " = ? AND (" + REPETITION_DATE + " <= ? OR " + REPETITION_DATE + " IS NULL)";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(category_id), dateString };
        Cursor cursor = db.rawQuery(queryString, params);
        String cardData;
        if (cursor.moveToFirst()) {
            String checkNull = cursor.getString(4);
            if (Objects.isNull(checkNull)) {
                cardData = cursor.getInt(0) + "#" + cursor.getString(1) + "#" + cursor.getString(2) + "#" + cursor.getInt(3) + "#0#0#0#0";
            } else {
                cardData = cursor.getInt(0) + "#" + cursor.getString(1) + "#" + cursor.getString(2) + "#" + cursor.getInt(3) + "#" + cursor.getInt(4) + "#" + cursor.getInt(5) + "#" + cursor.getInt(6) + "#" + cursor.getString(7);
            }
        } else {
            cardData = "No cards";
        }
        cursor.close();
        return cardData;
    }
    public int updateCard(String card_id, String correct, String ef, String interval, String date){
        String queryString = "UPDATE " + CARD + " SET " + CORRECT_ANSWERS + " = ?, " + EASINESS_FACTOR + " = ?, " + REPETITION_INTERVAL + " = ?, " + REPETITION_DATE + " = ? WHERE " + CARD_ID + " = ?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CORRECT_ANSWERS, Integer.parseInt(correct));
        cv.put(EASINESS_FACTOR, Float.parseFloat(ef));
        cv.put(REPETITION_INTERVAL, Integer.parseInt(interval));
        cv.put(REPETITION_DATE, date);
        int success = db.update(CARD, cv, CARD_ID + " = ?", new String[]{card_id});
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getCategoryProgress(int categoryId){
        String queryString = "SELECT * FROM " + CARD + " WHERE " + CATEGORY_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(categoryId) };
        Cursor cursor = db.rawQuery(queryString, params);
        String progress = "";
        if (cursor.moveToFirst()) {
            do {
                if (Objects.isNull(cursor.getInt(4))) {
                    continue;
                }
                progress += cursor.getInt(0) + "#" + cursor.getInt(4) + "#" + cursor.getFloat(5) + "#" + cursor.getInt(6) + "#" + cursor.getString(7) + ";";
            } while (cursor.moveToNext());
        }
        progress = progress.substring(0, progress.length() - 1);
        cursor.close();
        return progress;
    }

    

}
