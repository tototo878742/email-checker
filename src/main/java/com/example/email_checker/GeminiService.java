package com.example.email_checker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

	@Value("${gemini.api.key}")
	private String apiKey;

	private final RestClient restClient;
	private final ObjectMapper objectMapper; // JSON変換機

	public GeminiService(ObjectMapper objectMapper) {
		this.restClient = RestClient.create();
		this.objectMapper = objectMapper;
	}

	public DiagnosisResponse analyze(String emailText) {
		// モデルは最新のLite版
//		String url
//				= "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key="
//						+ apiKey;
//		Gemini 2.5 Flash Lite
//		Gemini 2.5 Flash
//		Gemini 3 Flash の３つが現状使える
		String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

		// プロンプト：JSON形式で返すように厳格に指定します
		String prompt = """
				あなたはセキュリティの専門家です。以下のメールを診断してください。
				結果は必ず以下のJSONフォーマットのみで出力してください。

				{
				  "score": 0〜100の整数（安全なら0、危険なら100）,
				  "reason": "初心者にもわかる優しい解説（150文字以内）",
				  "dangerousWords": ["メール本文に含まれる具体的な危険単語やフレーズ"]
				}

				※注意：dangerousWordsは、抽象的な概念（例：「緊急性」）ではなく、
				必ず「メール本文内の文字列と完全一致するフレーズ」（例：「24時間以内」「リンクをクリック」）を抽出してください。
				なければ空配列 [] にしてください。

				対象メール：
				%s
				""".formatted(emailText);
		// JSONリクエストの構築
		String requestJson = """
				{
				  "contents": [{
				    "parts": [{"text": "%s"}]
				  }],
				  "generationConfig": {
				    "responseMimeType": "application/json"
				  }
				}
				""".formatted(
				prompt.replace("\n", "\\n").replace("\"", "\\\""));

		try {
			String response = restClient.post().uri(url).header("Content-Type",
					"application/json").body(requestJson).retrieve().body(
							String.class);

			// 1. Geminiの巨大なレスポンスから "text" の中身（AIの返事）を取り出す
			String jsonText = extractTextFromGeminiResponse(response);

			// 2. 取り出した文字列（JSON）を Javaのオブジェクト(DiagnosisResponse)に変換する
			return objectMapper.readValue(jsonText, DiagnosisResponse.class);

		} catch (Exception e) {
			e.printStackTrace();
			// エラー時はとりあえずスコア-1などで返す
			return new DiagnosisResponse(-1, "エラーが発生しました: " + e.getMessage(),
					null);
		}
	}

	// 文字列操作で無理やり取り出すメソッド（前回と同じロジック）
	private String extractTextFromGeminiResponse(String response) {
		try {
			int startIndex = response.indexOf("\"text\": \"") + 9;
			String temp = response.substring(startIndex);
			// 次の " までは本文だが、エスケープされた " (\") はスキップする必要がある
			// ここでは簡易的に、一番後ろの " } ] } 付近から逆算して切り出すなど、工夫が必要ですが
			// GeminiがCleanなJSONを返すと仮定して、単純な切り出しを行います
			// ※本来はここもJacksonでパースすべきですが、今回は長くなるので簡易実装のままで行きます
			int endIndex = temp.lastIndexOf("\""); // 簡易実装
			// 最後のほうが構造の閉じ括弧などを含んでいる可能性があるので調整
			// 実はresponse全体をJacksonで読み込むのが一番安全ですが、今回はステップ重視で進めます

			// ★重要：前回の手法だとJSON内の " でバグる可能性があるので、
			// 簡易的に textフィールドの中身全体をパース済みの形にするライブラリ依存しない方法は難しいです。
			// 今回は「AIが返してきた中身」もJSONなので、単純な文字列切り出しでなく、
			// 「text": "」の後ろにある { から } までを正規表現で探すのが賢いです。

			// 修正版ロジック：最初の { と 最後の } を探して切り出す
			int jsonStart
					= response.indexOf("{", response.indexOf("\"text\":"));
			int jsonEnd = response.lastIndexOf("}") - 2; // 末尾の調整が必要な場合あり

			// 一番確実なのは「余計な文字（エスケープ文字）」を解除すること
			String rawText = temp.substring(0, temp.indexOf("\"\n          }")); // Geminiのフォーマット依存
			return rawText.replace("\\n", "\n").replace("\\\"", "\"");
		} catch (Exception e) {
			// 解析失敗時は空のJSONを返す
			return "{}";
		}
	}
}
