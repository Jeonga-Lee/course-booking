package com.example.lectureregistration.lecture.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.lectureregistration.lecture.application.domain.RegistrationHistory;
import com.example.lectureregistration.lecture.application.domain.RegistrationHistoryRepository;
import com.example.lectureregistration.lecture.application.domain.Lecture;
import com.example.lectureregistration.lecture.application.LectureService;
import com.example.lectureregistration.lecture.application.domain.LectureRepository;
import com.example.lectureregistration.lecture.exception.DuplicatedRegistrationException;
import com.example.lectureregistration.lecture.exception.EntityNotFoundException;
import com.example.lectureregistration.lecture.exception.ExcessCapacityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

	@InjectMocks
	LectureService lectureService;
	@Mock
	private LectureRepository lectureRepository;
	@Mock
	private RegistrationHistoryRepository historyRepository;

	private Long userId;
	private Lecture lecture1;
	private Lecture lecture2;
	private RegistrationHistory history;
	private String date;
	private LocalDate parsedDate;

	@BeforeEach
	void setUp() {
		Long lectureId = 1L;
		userId = 10L;
		date = "2024-12-23";
		parsedDate = LocalDate.parse(date,
			DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		lecture1 = Lecture.builder()
			.lectureName("특강")
			.isClosed(false)
			.instructorName("이정아")
			.date(parsedDate)
			.build();
		lecture1.setId(lectureId);

		lecture2 = Lecture.builder()
			.lectureName("특강2")
			.isClosed(false)
			.instructorName("이정아")
			.date(parsedDate)
			.build();

		history = RegistrationHistory.builder()
			.lecture(lecture1)
			.userId(userId)
			.build();
	}

	@DisplayName("날짜에 따라 특강 목록을 조회할 수 있다.")
	@Test
	void successGetLectures() {
	    //given
		List<Lecture> lectures = Arrays.asList(lecture1, lecture2);
		when(lectureRepository.findAllByDate(parsedDate)).thenReturn(lectures);

		//when
		List<Lecture> result = lectureService.getLecturesByDate(date);

		//then
		assertEquals("특강2", result.getLast().getLectureName());
		verify(lectureRepository, times(1)).findAllByDate(parsedDate);
	 }

	@DisplayName("해당 날짜에 신청 가능한 특강이 없을 때 EntityNotFoundException 발생한다.")
	@Test
	void failGetLecturesWhenEmpty() {
		 //when
		 Exception exception = assertThrows(EntityNotFoundException.class, () -> {
		 	lectureService.getLecturesByDate(date);
		 });

		 //then
		 assertEquals("조회된 특강이 없습니다.", exception.getMessage());
		 verify(lectureRepository, times(1)).findAllByDate(parsedDate);
	 }

	@DisplayName("특강 잔여 인원이 남아있으면 신청에 성공한다.")
	@Test
	void successAddRegistration() {
		//given
		when(historyRepository.save(any(RegistrationHistory.class))).thenReturn(history);

		//when
		RegistrationHistory savedHistory = lectureService.addRegistrationOfLecture(lecture1, userId);

		//then
		assertEquals(userId, savedHistory.getUserId());
		verify(historyRepository, times(1)).save(any(RegistrationHistory.class));
	}

	@DisplayName("특강 잔여 인원이 남아있지 않으면 ExcessCapacityException 발생한다.")
	@Test
	void failAddRegistrationWhenNoCapacity() {
		// given
		List<RegistrationHistory> histories = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			histories.add(history);
		}
		when(historyRepository.findHistoriesByLectureIdWithLock(lecture1.getId())).thenReturn(histories);

		// when
		ExcessCapacityException exception = assertThrows(ExcessCapacityException.class, () -> {
			lectureService.addRegistrationOfLecture(lecture1, userId);
		});

		// then
		assertEquals("수강 정원이 초과되었습니다.", exception.getMessage());
		verify(historyRepository).findHistoriesByLectureIdWithLock(lecture1.getId());
		verify(historyRepository, never()).save(any(RegistrationHistory.class));
	}


	@DisplayName("동일한 유저가 이미 신청한 강의를 중복 신청하면 DuplicateBookingException 발생한다.")
	@Test
	void failAddRegistrationWhenUserDuplicate() {
		//given
		when(historyRepository.save(any(RegistrationHistory.class))).thenReturn(history);
		lectureService.addRegistrationOfLecture(lecture1, userId);

		//when
		Exception exception = assertThrows(DuplicatedRegistrationException.class, () -> {
			when(historyRepository.existsByUserId(userId)).thenReturn(1);
			lectureService.addRegistrationOfLecture(lecture1, userId);
		});

		assertEquals("이미 신청한 특강입니다.", exception.getMessage());
		verify(historyRepository, times(1)).save(any(RegistrationHistory.class));
	}

	@DisplayName("특정 유저의 특강 신청 완료 목록 조회한다.")
	@Test
	void successGetHistoriesByUserId() {
		//given
		List<RegistrationHistory> histories = Arrays.asList(history, history);
		when(historyRepository.findHistoriesByUserId(userId)).thenReturn(histories);

		//when
		lectureService.getHistoriesByUserId(userId);

		//then
		assertEquals(2, histories.size());
		assertEquals(10L, histories.getFirst().getUserId());
		verify(historyRepository, times(1)).findHistoriesByUserId(any(Long.class));
	}

	@DisplayName("특정 유저의 특강 신청 완료 목록이 없을 때 EntityNotFoundException 발생한다.")
	@Test
	void failGetHistoriesWhenNoRegistrationByUserId() {
		//when
		Exception exception = assertThrows(EntityNotFoundException.class, () -> {
			lectureService.getHistoriesByUserId(userId);
		});

		//then
		assertEquals("조회된 특강이 없습니다.", exception.getMessage());
		verify(historyRepository, times(1)).findHistoriesByUserId(any(Long.class));
	}
}