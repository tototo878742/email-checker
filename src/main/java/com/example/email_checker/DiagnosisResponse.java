package com.example.email_checker;

import java.util.List;

// AIからの返信をここにマッピングします
// score: 危険度 (0-100)
// reason: 判定理由
// dangerousWords: 危険な単語のリスト（次のステップで使います）
public record DiagnosisResponse(int score, String reason, List<String> dangerousWords) {
}