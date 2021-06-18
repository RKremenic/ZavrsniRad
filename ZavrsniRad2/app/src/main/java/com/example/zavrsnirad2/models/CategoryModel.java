package com.example.zavrsnirad2.models;

public class CategoryModel {
    private int categoryId;
    private String categoryName;

    public CategoryModel(int category_id, String category_name) {
        this.categoryId = category_id;
        this.categoryName = category_name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
