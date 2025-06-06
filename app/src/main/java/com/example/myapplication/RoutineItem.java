package com.example.myapplication;

public class RoutineItem {
    public String title;
    public String thumbnailUrl;
    public String youtubeLink; // 🔹 유튜브 원본 링크 추가

    public RoutineItem(String title, String thumbnailUrl, String youtubeLink) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.youtubeLink = youtubeLink;
    }
}