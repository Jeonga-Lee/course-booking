package com.example.lectureregistration.lecture.interfaces;

import com.example.lectureregistration.lecture.application.domain.RegistrationHistory;
import com.example.lectureregistration.lecture.application.domain.Lecture;
import com.example.lectureregistration.lecture.application.LectureService;
import com.example.lectureregistration.lecture.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/lecture")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	@GetMapping()
	public List<Lecture> getLectures(@RequestParam String date) {
		return lectureService.getLecturesByDate(date);
	}

	@GetMapping("/user/{userId}")
	public List<RegistrationHistory> getHistoriesByUserId(
		@PathVariable Long userId
	) {
		return lectureService.getHistoriesByUserId(userId);
	}

	@PostMapping("/{lectureId}/registration")
	public RegistrationHistory addRegistrationOfLecture(
		@PathVariable Long lectureId,
		@RequestParam Long userId
	) {
		Lecture lecture = lectureService.getLectureById(lectureId)
			.orElseThrow(EntityNotFoundException::new);

		return lectureService.addRegistrationOfLecture(lecture, userId);
	}
}
