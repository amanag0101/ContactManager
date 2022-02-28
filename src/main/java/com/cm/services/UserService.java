package com.cm.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.cm.dao.UserRepository;
import com.cm.entity.User;

@Component
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	// get all the users
	public List<User> getAllUsers() {
		return userRepository.findAll();	
	}
	
	// get user by id
	public User getUserById(int id) {
		User user = userRepository.findById(id);
		return user;
	}
	
	// add a user
	public User addUser(User user) {
		if(user.getName() == null || user.getName().length() == 0) {
			return null;
		}
		else if(user.getEmail() == null || user.getEmail().length() == 0) {
			return null;
		}
		else if(user.getPassword() == null || user.getPassword().length() == 0) {
			return null;
		}
		
		User userFromDb = userRepository.findByEmail(user.getEmail());
		if(userFromDb != null) {
			return null;
		}
		
		user.setImageUrl("contact.png");
		user.setContacts(null);
		user.setSocialmediaLinks(null);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User result = userRepository.save(user);
		
		return result;
	}
	
	// update user by id
	public boolean updateBook(int id, User user) {
		if(user.getId() == id) {
			userRepository.save(user);
			return true;
		}
		
		return false;
	}
	
	// delete user by id
	public boolean deleteUserByEmail(String email) {
		User user = userRepository.findByEmail(email);
		
		if(user == null) {
			return false;
		}
		
		userRepository.delete(user);
		return true;
	}
}
