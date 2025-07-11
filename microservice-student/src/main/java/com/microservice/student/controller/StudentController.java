package com.microservice.student.controller;

import com.microservice.student.entities.Student;
import com.microservice.student.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

  @Autowired
  private IStudentService studentService;

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public void saveStudent(@RequestBody Student student) {
    studentService.save(student);
  }

  @GetMapping("/all")
  public ResponseEntity<?> findById() {
    return ResponseEntity.ok(studentService.findAll());
  }

  @GetMapping("/search/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) {
    return ResponseEntity.ok(studentService.findById(id));
  }

  @GetMapping("/search-by-course/{courseId}")
  public ResponseEntity<?> findByIdCourse(@PathVariable Long courseId) {
    return ResponseEntity.ok(studentService.findByCourseId(courseId));
  }

  @PutMapping("/{id}") // Endpoint para actualizar un estudiante
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
    // Aquí deberías añadir lógica para buscar el estudiante por ID, actualizar sus
    // campos
    // con los datos recibidos y luego guardarlo.
    // Para simplificar, asumimos que studentService.save(student) maneja la
    // actualización
    // si el ID ya existe. Si no, necesitas un método update en tu servicio.
    Student existingStudent = studentService.findById(id).orElse(null);
    if (existingStudent == null) {
      return ResponseEntity.notFound().build();
    }
    // Copiar propiedades (ej. con BeanUtils.copyProperties o manualmente)
    existingStudent.setName(student.getName());
    existingStudent.setLastName(student.getLastName());
    existingStudent.setEmail(student.getEmail());
    existingStudent.setCourseId(student.getCourseId());
    studentService.save(existingStudent); // Guarda el estudiante actualizado
    return ResponseEntity.ok("Estudiante actualizado exitosamente");
  }

  @DeleteMapping("/{id}") // Endpoint para eliminar un estudiante
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteStudentById(@PathVariable Long id) {
    studentService.deleteById(id);
  }
}