package com.microservice.student.service;

import com.microservice.student.entities.Student;

import java.util.List;
import java.util.Optional;

public interface IStudentService {

  List<Student> findAll();

  Optional<Student> findById(Long id);

  void save(Student student);

  List<Student> findByCourseId(Long courseId);

  void deleteById(Long id);

}