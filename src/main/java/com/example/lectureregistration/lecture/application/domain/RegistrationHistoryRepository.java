package com.example.lectureregistration.lecture.application.domain;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RegistrationHistoryRepository extends JpaRepository<RegistrationHistory, Long> {

	@Query("select h from RegistrationHistory h where userId =:userId")
	List<RegistrationHistory> findHistoriesByUserId(@Param("userId") Long userId);

	@Query("select count(h) from RegistrationHistory h where lecture.id =:lectureId")
	int findHistoriesCountByLectureId(@Param("lectureId") Long lectureId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select h from RegistrationHistory h where lecture.id = :lectureId")
	List<RegistrationHistory> findHistoriesByLectureIdWithLock(@Param("lectureId") Long lectureId);

	@Query("select count(h) from RegistrationHistory h where h.userId = :userId")
	int existsByUserId(@Param("userId") Long userId);

	@Modifying
	@Transactional
	@Query("delete from RegistrationHistory h where h.lecture.id = :lectureId")
	void deleteByLectureId(@Param("lectureId") Long lectureId);
}
