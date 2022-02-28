package com.cm.controller;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cm.dao.ContactRepository;
import com.cm.dao.SocialmediaLinkRepository;
import com.cm.dao.UserRepository;
import com.cm.entity.Contact;
import com.cm.entity.SocialmediaLinks;
import com.cm.entity.User;
import com.cm.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private SocialmediaLinkRepository socialmediaLinkRepository;
	
	@RequestMapping("/test")
	@ResponseBody
	public String test(Principal principal) {
		String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		List<Contact> contacts = user.getContacts();
		System.out.println("Size: " + contacts.size());
		System.out.println(contacts.toString());
		
		return "working";
	}
	
	// adding common data to the response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		m.addAttribute("user", user);
	}
	
	// dashboard
	@RequestMapping("/dashboard/{page}")
	public String dashboard(@PathVariable("page") Integer page, Model m, Principal principal) {
		final String pageTitle = "Dashboard";
		
		String username = principal.getName();
		User user = userRepository.findByEmail(username);
	
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contactIdList = contactRepository.findContactsByUser(user.getId(), pageable);
		List<User> contactList = new ArrayList<>();
		for(Contact contact: contactIdList) {
			User userContact = userRepository.findById(contact.getContactId());
			contactList.add(userContact);
		}
		
		m.addAttribute("pageTitle", pageTitle);
		m.addAttribute("contacts", contactList);
		m.addAttribute("contactsSize", contactList.size());
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contactIdList.getTotalPages());
		
		return "/user/dashboard";
	}
	
	// show complete contact details
	@RequestMapping("/{id}/contact")
	public String showContactDetails(@PathVariable("id") Integer contactId, Model m, Principal principal) {
		final String pageTitle = "Contact Details";
	
		// logged-in user
		final String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		// contact of type (User)
		final Optional<User> optional = userRepository.findById(contactId);
		final User contact = optional.get();
		final List<SocialmediaLinks> links = contact.getSocialmediaLinks();
		
		// for securing url
		Contact c = contactRepository.findByContactIdAndUserId(contactId, user.getId());
		if(c != null)
			m.addAttribute("contact", contact);
		
		m.addAttribute("pageTitle", pageTitle);
		m.addAttribute("socialLinks", links);
		
		return "/user/contactDetails";
	}
	
	// delete contact
	@RequestMapping("/delete/{id}")
	public String deleteContact(@PathVariable("id") Integer contactId, Model m, Principal principal) {
		// logged-in user
		final String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		// contact of type (Contact)
		Contact contact = contactRepository.findByContactIdAndUserId(contactId, user.getId());
		if(contact != null)
			contactRepository.delete(contact);
		
		return "redirect:/user/dashboard/0";
	}
	
	// addContactForm page
	@RequestMapping("/add-contact") 
	public String addContact(Model m, Principal principal) {
		final String pageTitle = "Add Contact";	
		m.addAttribute("pageTitle", pageTitle);
		
		return "/user/addContactForm";
	}
	
	// on click of add button 
	@PostMapping("/add-contact")
	public String checkAddContactInfo(@RequestParam(value = "email") String email, HttpSession session, Principal principal) {
		// logged in user
		String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		// contact to add
		User contactWithEmail = userRepository.findByEmail(email);
		
		// check if the contact exists
		if(contactWithEmail == null) {
			session.setAttribute("message", new Message("Email does not exist.", "error"));
			return "/user/addContactForm";
		}
		
		// check if contact is not same as the user
		if(user.getId() == contactWithEmail.getId()) {
			session.setAttribute("message", new Message("You can't add yourself as an contact.", "error"));
			return "/user/addContactForm";
		}
		
		// check if the contact is already added or not
		Contact c = contactRepository.findByContactIdAndUserId(contactWithEmail.getId(), user.getId());
		if(c == null) {
			Contact contact = new Contact();
			contact.setContactId(contactWithEmail.getId());
			contact.setUser(user);
			
			//user.getContacts().add(contact);
			
			// adding contact to the database
			contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact added!", "success"));
		}
		else {
			session.setAttribute("message", new Message("Contact already added!", "error"));
		}
		
		return "/user/addContactForm";
	}

	// edit profile page
	@RequestMapping("/edit-profile")
	public String editProfile(Model m, Principal principal) {	
		final String pageTitle = "Edit Profile";
		
		final String username = principal.getName();
		final User user = userRepository.findByEmail(username);

		final List<SocialmediaLinks> socialLinks = user.getSocialmediaLinks();
		
		m.addAttribute("pageTitle", pageTitle);
		m.addAttribute("socialLinks", socialLinks);
		
		return "/user/editProfile";
	}
	
	@PostMapping("/update-profile")
	public String processEditProfileInfo(@ModelAttribute("user") User user, @RequestParam(value = "profileImage") MultipartFile userImageFile, 
			@RequestParam(value = "socialmediaBrand") String socialmediaBrand, 
			@RequestParam(value = "socialmediaLink") String socialmediaLink) {
		// uploading file
		if(!userImageFile.isEmpty()) {
			try {
				File file = new ClassPathResource("/static/img").getFile();
				//Path path = Paths.get(file.getAbsolutePath() + File.separator + userImageFile.getOriginalFilename());
				Path path = Paths.get(file.getAbsolutePath() + File.separator + user.getId());
				Files.copy(userImageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				user.setImageUrl(user.getId() + "");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Social Media
		SocialmediaLinks socialmediaLinks = new SocialmediaLinks();
		if(socialmediaBrand.length() != 0 && socialmediaLink.length() != 0) {
			socialmediaLinks.setUser(user);
			socialmediaLinks.setBrand(socialmediaBrand);
			socialmediaLinks.setProfileUrl(socialmediaLink);
			
			//user.getSocialmediaLinks().add(socialmediaLinks);
			
			// updating social media
			socialmediaLinkRepository.save(socialmediaLinks);
		}
		
		/*
		 * List<SocialmediaLinks> list = user.getSocialmediaLinks();
		 * for(SocialmediaLinks link : list) System.out.println(link);
		 */
		
		// updating the user
		userRepository.save(user);
		
		return "redirect:/user/edit-profile";
	}
	
	// delete a social media link
	@RequestMapping("/delete-link/{id}")
	public String deleteLink(@PathVariable("id") Integer id, Principal principal) {
		final String username = principal.getName();
		final User user = userRepository.findByEmail(username);
		
		Optional<SocialmediaLinks> optional = socialmediaLinkRepository.findById(id);
		
		if(optional != null) {
			SocialmediaLinks link = optional.get();
			
			if(link.getUser().getId() == user.getId()) {
				socialmediaLinkRepository.delete(link);
			}
		}
		
		return "redirect:/user/edit-profile";
	}
	
	// View Profile page
	@RequestMapping("/view-profile")
	public String viewProfile(Model m, Principal principal) {
		final String pageTitle = "View Profile";
		
		final String username = principal.getName();
		final User user = userRepository.findByEmail(username);

		final List<SocialmediaLinks> socialLinks = user.getSocialmediaLinks();
		
		m.addAttribute("pageTitle", pageTitle);
		m.addAttribute("socialLinks", socialLinks);
		
		return "/user/viewProfile";
	}
}
