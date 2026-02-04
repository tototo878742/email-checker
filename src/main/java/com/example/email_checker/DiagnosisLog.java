package com.example.email_checker;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // これをつけるとDBのテーブルになります
@Table(name = "diagnosis_logs")
public class DiagnosisLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5000) // メール本文は長いので制限を緩める
    private String originalText;

    private int score;
    private String reason;
    private LocalDateTime createdAt;

    // コンストラクタ（保存するときに使います）
    public DiagnosisLog() {} // JPA用に空のが必要

    public DiagnosisLog(String originalText, int score, String reason) {
        this.originalText = originalText;
        this.score = score;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    // Getter (画面表示に使います)
    public String getOriginalText() { return originalText; }
    public int getScore() { return score; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // メール本文が長すぎる時に省略表示する便利メソッド
    public String getShortText() {
        return originalText.length() > 50 ? originalText.substring(0, 50) + "..." : originalText;
    }
}