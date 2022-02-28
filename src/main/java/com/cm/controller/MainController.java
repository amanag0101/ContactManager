package com.cm.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cm.dao.UserRepository;
import com.cm.entity.User;
import com.cm.helper.Message;

@Controller
public class MainController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/test")
	@ResponseBody
	public String test() {
		User user = new User();
		user.setName("Aman");
		user.setEmail("aman@gmail.com");
		user.setPassword(passwordEncoder.encode("aman"));
		user.setPhone("1234567890");
		user.setAbout("about");
		user.setImageUrl("contact.png");
		
		User result = userRepository.save(user);
		System.out.println(result);
		
		return "working";
	}
	
	// index page
	@RequestMapping("/")
	public String index() {
		return "index";
	}
	
	// login page
	@RequestMapping("/login") 
	public String login() {
		return "index";
	}
	
	// register page
	@RequestMapping("/register")
	public String register(Model m) {
		m.addAttribute("user", new User());
		return "register";
	}
	
	// on click of the register button
	@PostMapping("/register")
	public String checkUserInfo(
			@ModelAttribute("user") User user, 
			@RequestParam(value = "passwordRepeat") String passwordRepeat,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			HttpSession session) {
		
		// check agreement
		if(!agreement) {
			session.setAttribute("message", new Message("Please accept the Terms and Conditions.", "error"));
			return "register";
		}
		
		// check if any field is empty or not
		if(user.getName() == null || user.getName().length() == 0) {
			session.setAttribute("message", new Message("Please enter your name.", "error"));
			return "register";
		}
		else if(user.getEmail() == null || user.getEmail().length() == 0) {
			session.setAttribute("message", new Message("Please enter your email.", "error"));
			return "register";
		}
		else if(user.getPassword() == null || user.getPassword().length() == 0) {
			session.setAttribute("message", new Message("Please enter a password.", "error"));
			return "register";
		}
		
		// check if account with same email exists or not
		String checkEmail = user.getEmail();
		User userWithEmail = userRepository.findByEmail(checkEmail);
		
		if(userWithEmail != null) {
			session.setAttribute("message", new Message("Email already exist.", "error"));
			return "register";
		}
		
		// check password and password repeat
		if(!user.getPassword().equals(passwordRepeat)) {
			session.setAttribute("message", new Message("Password does not match.", "error"));
			return "register";
		}
		
		// encrypting the password
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		// add user to the database
		userRepository.save(user);
		
		return "redirect:/";
	}
}

