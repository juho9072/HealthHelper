package com.example.myapplication;

public class Routine {
    private int id;
    private String name;
    private String description;
    private String tag;

    public Routine(int id, String name, String description, String tag) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tag = tag;
    }

    // Getter 메서드들
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTag() {
        return tag;
    }

    // (Optional) toString()
    @Override
    public String toString() {
        return name + " - " + tag;
    }
}
