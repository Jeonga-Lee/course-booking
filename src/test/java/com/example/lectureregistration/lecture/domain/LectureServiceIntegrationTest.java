package com.example.lectureregistration.lecture.domain;

import com.example.lectureregistration.lecture.application.LectureService;
import com.example.lectureregistration.lecture.application.domain.Lecture;
import com.example.lectureregistration.lecture.application.domain.LectureRepository;
import com.example.lectureregistration.lecture.application.domain.RegistrationHistoryRepository;
import com.example.lectureregistration.lecture.exception.DuplicatedRegistrationException;
import com.example.lectureregistration.lecture.exception.ExcessCapacityException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LectureServiceIntegrationTest {

	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureRepository lectureRepository;
	@Autowired
	private RegistrationHistoryRepository historyRepository;

	@Test
	@DisplayName("40명이 신청했을 때 30명 이상 신청이 들어오지 않도록 동시성 제어 및 통합 테스트 작성.")
	void testAddRegistrationWhenNoCapacity() throws InterruptedException {
		// given
		Lecture lecture = Lecture.builder()
			.lectureName("테스트 강의")
			.isClosed(false)
			.instructorName("이정아")
			.date(LocalDate.of(2024, 12, 23))
			.build();
		lectureRepository.save(lecture);

		// when
		ExecutorService executor = Executors.newFixedThreadPool(10);
		CountDownLatch latch = new CountDownLatch(1);

		Long userId = 1L;
		for (int i = 1; i <= 40; i++) {
			final Long currentUserId = userId;  // currentUserId로 복사하여 final로 캡처
			Runnable task = () -> {
				try {
					latch.await();
					lectureService.addRegistrationOfLecture(lecture, currentUserId);  // 캡처된 unique userId 사용
				} catch (ExcessCapacityException | InterruptedException e) {
					assertInstanceOf(ExcessCapacityException.class, e, "수강 정원이 초과되었습니다.");
				}
			};
			executor.submit(task);
			userId++;
		}

		latch.countDown();

		executor.shutdown();
		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		if (!finished) {
			fail("시간 초과");
		}

		// then
		int size = historyRepository.findHistoriesCountByLectureId(lecture.getId());
		assertEquals(30, size, "수강 신청 수가 30명이 되어야 합니다.");
		historyRepository.deleteByLectureId(lecture.getId());
	}


	@Test
	@DisplayName("동일한 유저 정보로 같은 특강을 신청했을 때 1번만 성공한다.")
	void testDuplicatedRegistration() throws InterruptedException {
		//given
		Long userId = 10L;
		Lecture lecture = Lecture.builder()
			.lectureName("테스트 강의")
			.isClosed(false)
			.instructorName("이정아")
			.date(LocalDate.of(2024, 12, 23))
			.build();
		lectureRepository.save(lecture);

		//when
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(1);

		for (int i = 0; i < 2; i++) {
			Runnable task = () -> {
				try {
					latch.await();
					lectureService.addRegistrationOfLecture(lecture, userId);
				} catch (DuplicatedRegistrationException | InterruptedException e) {
					assertInstanceOf(DuplicatedRegistrationException.class, e, "이미 신청한 특강입니다.");
				}
			};
			executor.submit(task);
		}
		latch.countDown();

		executor.shutdown();
		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		if (!finished) {
			fail("시간 초과");
		}

		// then
		int size = historyRepository.findHistoriesCountByLectureId(lecture.getId());
		assertEquals(1, size, "1번만 수강 신청 되어야 합니다.");
		historyRepository.deleteByLectureId(lecture.getId());
	}


}
