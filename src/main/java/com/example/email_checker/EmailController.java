package com.example.email_checker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class EmailController {

	private final GeminiService geminiService;
	private final DiagnosisLogRepository repository; // 追加

	// コンストラクタでRepositoryも受け取る
	public EmailController(GeminiService geminiService,
			DiagnosisLogRepository repository) {
		this.geminiService = geminiService;
		this.repository = repository;
	}

	@GetMapping("/")
	public String index(Model model) {
		// トップページを開いた時、過去の履歴を取得して渡す
		List<DiagnosisLog> history
				= repository.findTop5ByOrderByCreatedAtDesc();
		model.addAttribute("historyList", history);
		return "index";
	}

	@PostMapping("/analyze")
	public String analyze(@RequestParam("emailText") String emailText,
			Model model) {
		DiagnosisResponse result = geminiService.analyze(emailText);

		// ★ここでデータベースに保存！
		DiagnosisLog log
				= new DiagnosisLog(emailText, result.score(), result.reason());
		repository.save(log);

		// 結果表示
		model.addAttribute("score", result.score());
		model.addAttribute("reason", result.reason());
		model.addAttribute("original", emailText);
		model.addAttribute("dangerousWords", result.dangerousWords());

		// 診断後も履歴を見たいので、再度取得して渡す
		List<DiagnosisLog> history
				= repository.findTop5ByOrderByCreatedAtDesc();
		model.addAttribute("historyList", history);

		return "index";
	}
}
