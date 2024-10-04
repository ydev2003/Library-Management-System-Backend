package com.merck.library_management_system.controller;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.merck.library_management_system.entity.Book;
import com.merck.library_management_system.entity.Student;
import com.merck.library_management_system.repository.AdminRepository;
import com.merck.library_management_system.repository.BookRepository;
import com.merck.library_management_system.repository.StudentRepository;
import com.merck.library_management_system.security.JwtUtil;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	BookRepository bp;
	
	@Autowired
	StudentRepository sp;
	
	@Autowired
	AdminRepository ap;
	
	 @Autowired
	 private JwtUtil jwtUtil;
	
	
	@PutMapping("/issue")
	@Transactional
	public ResponseEntity<String> issue(@RequestParam Long bookId, HttpServletRequest request) {
	    // Extract the JWT token from the request header
	    String authorizationHeader = request.getHeader("Authorization");
	    String studentId = null;

	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        String jwt = authorizationHeader.substring(7); // Extract the token
	        studentId = jwtUtil.extractUsername(jwt); // Extract the username from the token
	    }

	    if (studentId == null) {
	    	 return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("There is no student with this username.");
	    }
	    
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
 	public ResponseEntity<String> submit(@RequestParam Long bookId, HttpServletRequest request ) {
 		
 		String authorizationHeader = request.getHeader("Authorization");
	    String studentId = null;

	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        String jwt = authorizationHeader.substring(7); // Extract the token
	        studentId = jwtUtil.extractUsername(jwt); // Extract the username from the token
	    }

	    if (studentId == null) {
	    	 return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("There is no student with this username.");
	    }
 		
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
 	
	@GetMapping("/book")
	public ResponseEntity<List<Book>> getStudentBookById(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
	    String studentId = null;

	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        String jwt = authorizationHeader.substring(7); // Extract the token
	        studentId = jwtUtil.extractUsername(jwt); // Extract the username from the token
	    }

	    if (studentId == null) {
	        return null; // Handle case where student ID is not found
	    }
		
	    if(sp.existsById(studentId)) {
			 List<Book> books = sp.findById(studentId).get().getBookTaken();
			 return ResponseEntity.ok(books);
		}
		else
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	
	@PutMapping("/update")
	@Transactional
	public ResponseEntity<String> updateStudentById(@RequestParam String currentPassword, @RequestParam String newPassword, HttpServletRequest request ) {
		
		String authorizationHeader = request.getHeader("Authorization");
	    String studentId = null;

	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        String jwt = authorizationHeader.substring(7); // Extract the token
	        studentId = jwtUtil.extractUsername(jwt); // Extract the username from the token
	    }

	    if (studentId == null) {
	    	return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("There is no student with this username.");
	    }
	
	    Optional<Student> studentOpt = sp.findById(studentId);
	    if (studentOpt.isPresent()) {
	        Student student = studentOpt.get();
	        if (student.getPassword().equals(currentPassword)) {
	            student.setPassword(newPassword); // Update the password
	            sp.save(student); // Save the updated student back to the repository
	            return ResponseEntity.ok("Your password changed successfully.");
	    	}
	    	else {
	    		 return ResponseEntity.status(HttpStatus.CONFLICT).body("Please enter your correct current password.");
	    	}
	    }
	    else {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no student with this username.");
	    }
	    	
	}
}

