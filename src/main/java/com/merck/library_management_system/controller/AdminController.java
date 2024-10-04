package com.merck.library_management_system.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.merck.library_management_system.entity.Admin;
import com.merck.library_management_system.entity.Book;
import com.merck.library_management_system.entity.Student;
import com.merck.library_management_system.repository.AdminRepository;
import com.merck.library_management_system.repository.BookRepository;
import com.merck.library_management_system.repository.StudentRepository;

@CrossOrigin("*")
@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	BookRepository bp;
	
	@Autowired
	StudentRepository sp;
	
	@Autowired
	AdminRepository ap;
	
	
	@PutMapping("/issue")
	@Transactional
 	public ResponseEntity<String> issue (@RequestParam Long bookId, @RequestParam String studentId) {
 		
 		Student student=null;
 		
 		
 		if(bp.findById(bookId).isPresent()) {
 			Book book = bp.findById(bookId).get();
 			if(book.isAvailable()) {
 				if(sp.findById(studentId).isPresent()){
 					book.setAvailable(false);
 				
 				
 					book.setIssueDate(LocalDate.now());
 					book.setDueDate(LocalDate.now().plusDays(7));
 				
 					student = sp.findById(studentId).get();
 					student.getBookTaken().add(book);
 					bp.save(book);
 					sp.save(student);
 					return ResponseEntity.ok("Book issued successfully.");
 				}
 				else {
 					 return ResponseEntity.status(HttpStatus.CONFLICT).body("There is no student with this username.");
 				}
 			}
 			else {
 				 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Book is not available.");
 			}
 		}
 		else {
 			 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no book with this ID.");
 		}
 	}
 	
 	@PutMapping("/submit")
 	@Transactional
 	public ResponseEntity<String> submit(@RequestParam Long bookId, @RequestParam String studentId) {
 		
 		Student student=null;
 		if(bp.findById(bookId).isPresent()) {
 			
 			Book book = bp.findById(bookId).get();
 			
 			if(!book.isAvailable()) {
 				if(sp.findById(studentId).isPresent()){
 					student = sp.findById(studentId).get();
 					if(student.getBookTaken().contains(book)) {
 						book.setAvailable(true);
 						book.setIssueDate(null);
 						book.setDueDate(null);
 						student.getBookTaken().remove(book);
 						bp.save(book);
 	 					sp.save(student);
 	 					return ResponseEntity.ok("Book submited successfully.");
 	 				}
 					else {
 						 return ResponseEntity.status(HttpStatus.FORBIDDEN).body("There is not issued by this student.");
 					}
 				}
 	 			else {
 	 					 return ResponseEntity.status(HttpStatus.CONFLICT).body("There is no student with this username.");
 	 			}
 			}
 	 		else {
 	 			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This book is not issued to anyone.");
 	 		}
 		}
 	 	else {
 	 			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no book with this ID.");
 	 	}
 	}
 	
 	
 	@PostMapping("/admin")
 	public boolean addAdmin(@RequestBody Admin admin) {
 		String id = admin.getUsername();
		if(ap.existsById(id)) {
			return false;
		}
		else {
			ap.save(admin);
			return true;
		}
 		
 	}
}
