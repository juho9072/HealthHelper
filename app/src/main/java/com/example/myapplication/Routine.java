package com.example.myapplication;

public class Routine {
    private int id;
    private String name;
    private String description;
    private String tag;
    private String youtubeLink;

    public Routine(int id, String name, String description, String tag, String youtubeLink) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tag = tag;
        this.youtubeLink = youtubeLink;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getTag() { return tag; }

    public String getYoutubeLink() {return youtubeLink;}

}
