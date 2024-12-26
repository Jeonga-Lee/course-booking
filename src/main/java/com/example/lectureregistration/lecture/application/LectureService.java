package com.example.lectureregistration.lecture.application;

import com.example.lectureregistration.lecture.application.domain.RegistrationHistory;
import com.example.lectureregistration.lecture.application.domain.RegistrationHistoryRepository;
import com.example.lectureregistration.lecture.application.domain.Lecture;
import com.example.lectureregistration.lecture.application.domain.LectureRepository;
import com.example.lectureregistration.lecture.exception.DuplicatedRegistrationException;
import com.example.lectureregistration.lecture.exception.ExcessCapacityException;
import com.example.lectureregistration.lecture.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureService {

	@Autowired
	private final LectureRepository lectureRepository;
	@Autowired
	private final RegistrationHistoryRepository historyRepository;

	@Transactional(readOnly = true)
	public Optional<Lecture> getLectureById(Long lectureId) {
		return lectureRepository.findById(lectureId);
	}

	@Transactional(readOnly = true)
	public List<Lecture> getLecturesByDate(String date) {
		List<Lecture> lecture = lectureRepository.findAllByDate(LocalDate.parse(date,
			DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		if (lecture.isEmpty()) {
			throw new EntityNotFoundException();
		}
		return lecture;
	}

	@Transactional(readOnly = true)
	public List<RegistrationHistory> getHistoriesByUserId(Long userId) {
		List<RegistrationHistory> histories = historyRepository.findHistoriesByUserId(userId);
		if (histories.isEmpty()) {
			throw new EntityNotFoundException();
		}
		return histories;
	}

	public RegistrationHistory addRegistrationOfLecture(Lecture lecture, Long userId) {
		validateRegistration(lecture, userId);

		RegistrationHistory history = RegistrationHistory.builder().lecture(lecture).userId(userId).build();
		historyRepository.save(history);

		return history;
	}

	private void validateRegistration(Lecture lecture, Long userId) {
		if (historyRepository.existsByUserId(userId) > 0) {
			throw new DuplicatedRegistrationException();
		}

		int count = getRegistrationCount(lecture);

		if (count == 29) {
			lecture.setIsClosed(true);
		}

		if (count > 29) {
			throw new ExcessCapacityException();
		}
	}

	public int getRegistrationCount(Lecture lecture) {
		return historyRepository.findHistoriesByLectureIdWithLock(lecture.getId()).size();
	}

}
