package com.example.myapplication;

public class RoutineItem {
    public String title;
    public String thumbnailUrl;
    public String youtubeLink;
    public String difficulty; // 🔹 난이도 필드 추가

    public RoutineItem(String title, String thumbnailUrl, String youtubeLink, String difficulty) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.youtubeLink = youtubeLink;
        this.difficulty = difficulty;
    }

    // 기존 구조도 호환되게 하기 위해 오버로딩 생성자 추가
    public RoutineItem(String title, String thumbnailUrl, String youtubeLink) {
        this(title, thumbnailUrl, youtubeLink, ""); // 기본 난이도는 빈 값
    }
}