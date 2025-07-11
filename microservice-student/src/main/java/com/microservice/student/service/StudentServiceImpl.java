package com.microservice.student.service;

import com.microservice.student.entities.Student;
import com.microservice.student.persistence.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements IStudentService {

  @Autowired
  private StudentRepository studentRepository;

  @Override
  public List<Student> findAll() {
    return (List<Student>) studentRepository.findAll();
  }

  @Override
  public Optional<Student> findById(Long id) {
    // Usa directamente el método findById del repositorio que ya devuelve Optional
    return studentRepository.findById(id);
  }

  @Override
  public void save(Student student) {
    studentRepository.save(student);
  }

  @Override
  public List<Student> findByCourseId(Long courseId) {
    return studentRepository.findAllByCourseId(courseId);
  }

  @Override
  public void deleteById(Long id) {
    studentRepository.deleteById(id);
  }
}