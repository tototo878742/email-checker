# 🛡️ Email Guardian AI (あやしいメール診断くん)

新入社員やIT初心者をターゲットにした、フィッシング詐欺メールの危険度判定ツールです。
Google Gemini (AI) を活用し、メール本文のリスクスコア判定、危険なキーワードのハイライト、教育的な解説をリアルタイムで提供します。

**Demo URL:** https://email-checker.koyeb.app/
## 🚀 開発の背景
「知識がない人でも直感的にメールの危険性を判断できるツールが必要だ」と考え開発しました。
単に「危険」と判定するだけでなく、「なぜ危険なのか」をAIに解説させることで、ユーザーのリテラシー向上も狙っています。

## 🛠️ 技術スタック
### Backend
- **Java 17**
- **Spring Boot 4.0.2 (Web, JPA)
- **Google Gemini API** (Generative AI)

### Frontend
- **Thymeleaf** (Server-side rendering)
- **Bootstrap 5** (Responsive UI)
- **JavaScript** (DOM manipulation for highligting)

### Infrastructure & Database
- **Koyeb** (PaaS for Application deployment)
- **Neon** (Serverless PostgreSQL)
- **H2 Database** (Local development)

## ✨ 主な機能
1.  **AIリスク診断:** メール本文を解析し、0〜100%で危険度をスコアリング。
2.  **赤ペン先生機能:** 「急かし」「不審なURL」などの危険ワードを自動でハイライト表示。
3.  **診断履歴の共有:** 診断結果はクラウドデータベース(PostgreSQL)に保存され、チーム内で事例を共有可能。

## 🏗️ データベース設計
**Table: diagnosis_logs**
| Column | Type | Description |
| --- | --- | --- |
| id | Long | Primary Key |
| original_text | Text | メール本文（全文） |
| score | Integer | 危険度スコア (0-100) |
| reason | String | AIによる判定理由 |
| created_at | DateTime | 診断日時 |

## 👨‍💻 工夫した点
- **ハイブリッドな開発環境:** ローカルでは軽量なH2、本番では堅牢なNeon(PostgreSQL)と、環境に応じてDBを自動で切り替える構成にしました。
- **UX/UI:** 診断待ちのストレスを減らすローディング表示や、直感的なメーター表示など、ユーザー体験を重視しました。

## 📐 設計図 (Architecture)

### クラス図 (Class Diagram)
```mermaid
classDiagram
    %% クラスの定義
    class EmailController {
        <<Controller>>
        +index(Model) String
        +analyze(String, Model) String
    }

    class GeminiService {
        <<Service>>
        -String apiKey
        -String GEMINI_URL
        +analyzeEmail(String text) DiagnosisLog
        -callGeminiApi(String prompt) String
    }

    class DiagnosisLogRepository {
        <<Interface>>
        +findAll() List~DiagnosisLog~
        +save(DiagnosisLog) DiagnosisLog
    }

    class DiagnosisLog {
        <<Entity>>
        -Long id
        -String originalText
        -Integer score
        -String reason
        -LocalDateTime createdAt
    }

    %% 関係性の定義
    EmailController ..> GeminiService : 使用 (Uses)
    EmailController ..> DiagnosisLogRepository : 履歴取得 (Uses)
    GeminiService ..> DiagnosisLogRepository : 保存 (Uses)
    DiagnosisLogRepository ..> DiagnosisLog : 管理 (Manages)
```

### シーケンス図 (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor User as ユーザー
    participant View as ブラウザ(HTML)
    participant Ctrl as EmailController
    participant Svc as GeminiService
    participant API as Google Gemini API
    participant DB as Neon DB (PostgreSQL)

    %% 診断フロー開始
    User->>View: メール本文を入力して「診断」クリック
    View->>Ctrl: POST /analyze (本文送信)
    
    activate Ctrl
    Ctrl->>Svc: analyzeEmail(本文)
    
    activate Svc
    Note right of Svc: プロンプトの作成<br>(役割付与+JSON形式指定)
    
    Svc->>API: HTTP Request (POST)
    activate API
    API-->>Svc: JSON Response (スコア, 理由, 危険語句)
    deactivate API
    
    Note right of Svc: JSONの解析(パース)と<br>Entityオブジェクトの作成
    
    Svc->>DB: save(診断ログ)
    activate DB
    DB-->>Svc: 保存完了
    deactivate DB
    
    Svc-->>Ctrl: 診断結果(Entity)を返す
    deactivate Svc

    %% 履歴の再取得
    Ctrl->>DB: findAll() (過去の履歴取得)
    activate DB
    DB-->>Ctrl: 履歴リスト
    deactivate DB

    Ctrl-->>View: 結果画面を表示 (Thymeleaf)
    deactivate Ctrl
    
    View-->>User: スコアと解説を表示<br>(自動スクロール)
```
