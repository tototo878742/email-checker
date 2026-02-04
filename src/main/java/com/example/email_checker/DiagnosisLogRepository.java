package com.example.email_checker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisLogRepository extends JpaRepository<DiagnosisLog, Long> {
    // 最新の5件を取得するメソッドを定義
    // (Spring Data JPAが名前から自動でSQLを作ってくれます)
    java.util.List<DiagnosisLog> findTop5ByOrderByCreatedAtDesc();
}