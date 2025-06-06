package com.example.gachanexus;

public class Category {
    private int id;
    private String name;
    private String slug;
    private int count;

    public Category() {}

    public Category(int id, String name, String slug, int count) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.count = count;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public int getCount() { return count; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setCount(int count) { this.count = count; }
}