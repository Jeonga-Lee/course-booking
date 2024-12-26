package com.example.lectureregistration.lecture.application.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
	@Query("select lc from Lecture lc where lc.date = :date")
	List<Lecture> findAllByDate(LocalDate date);

}
