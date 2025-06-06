package com.example.gachanexus;

public class Post {
    private int id;
    private String title;
    private String content;
    private String excerpt;
    private String featuredImage;
    private String date;
    private String categoryName;
    private int categoryId;

    public Post() {}

    public Post(int id, String title, String content, String excerpt, String featuredImage, String date, String categoryName, int categoryId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.excerpt = excerpt;
        this.featuredImage = featuredImage;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getExcerpt() { return excerpt; }
    public String getFeaturedImage() { return featuredImage; }
    public String getDate() { return date; }
    public String getCategoryName() { return categoryName; }
    public int getCategoryId() { return categoryId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public void setFeaturedImage(String featuredImage) { this.featuredImage = featuredImage; }
    public void setDate(String date) { this.date = date; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}