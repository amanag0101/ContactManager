package com.cm.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cm.entity.User;
import com.cm.services.UserService;

@RestController
public class UserRestController {
	@Autowired
	private UserService userService;
	
	// get all the users
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getAllUsers();
		
		if(users.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		
		return ResponseEntity.of(Optional.of(users));
	}
	
	// get user by id
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
		User user = userService.getUserById(id);
		
		if(user == null)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		
		return ResponseEntity.of(Optional.of(user));
	}
	
	// add a user
	@PostMapping("/users")
	public ResponseEntity<User> addUser(@RequestBody User user) {	
		User result = userService.addUser(user);
		
		if(result == null)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	// update user by id
	@PutMapping("/users/{id}")
	public ResponseEntity<Void> updateUserById(@PathVariable("id") int id, @RequestBody User user) {
		try {
			if(userService.updateBook(id, user)) {
				return ResponseEntity.ok().build();
			}
			else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	// delete user by email
	@DeleteMapping("/users/{email}")
	public ResponseEntity<Void> deleteUserByEmail(@PathVariable("email") String email) {
		try {
			if(userService.deleteUserByEmail(email)) 
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			else
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
