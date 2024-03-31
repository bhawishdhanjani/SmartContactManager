package com.smart.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.naming.Binding;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void setModelData(Model model, Principal principal) {
		String email = principal.getName();
		User user = userRepository.getUserByUserName(email);
		User user2 = user.copyUser();
		model.addAttribute("user", user2);
	}

	@RequestMapping("/index")
	public String getDashboard(Model model) {
		model.addAttribute("title", "Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String getAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		Contact contact = new Contact();
		model.addAttribute("contact", contact);
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String saveContact(@ModelAttribute("contact") Contact contact, HttpSession session,
			@RequestParam("profileImage") MultipartFile multipartFile, Principal principal) {
		try {
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			contact.setUser(user);
			if (multipartFile.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(multipartFile.getOriginalFilename());
			}

			user.getContacts().add(contact);
			User result = userRepository.save(user);
			File file = new ClassPathResource("/static/images").getFile();
			Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
			Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			session.setAttribute("message", new Message("Contact added successfully!! Add more...", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing Went Wrong!! Please try again..", "dangers"));

		}
		return "normal/add_contact_form";

	}

	@GetMapping("show-contacts/{page}")
	public String showContact(Model model, @PathVariable("page") Integer page, Principal principal) {
		model.addAttribute("title", "Show Contacts");
		int userId = userRepository.getUserByUserName(principal.getName()).getId();

		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.getContactsByUserId(userId, pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	@RequestMapping("contact/{cId}")
	public String showContactDetail(@PathVariable("cId") int cId, Model model, Principal principal) {
		User user = userRepository.getUserByUserName(principal.getName());
		Contact contact = contactRepository.getReferenceById(cId);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", "Contact Details");
		}
		return "normal/contact_detail";
	}

	@GetMapping("delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId, HttpSession session, Principal principal) {
		Message message = new Message("Contact Deleted Successfully..", "success");
		User user = userRepository.getUserByUserName(principal.getName());
		Contact contact = contactRepository.getReferenceById(cId);
		if (user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
			contactRepository.delete(contact);
			session.setAttribute("message", message);
			return "redirect:/user/show-contacts/0";
		} else {
			session.setAttribute("message", new Message("Contact is not deleted..", "danger"));
			return "redirect:/user/show-contacts/0";
		}

	}

	@PostMapping("update-contact/{cId}")
	public String updateForm(@PathVariable("cId") int cId, Model model, Principal principal) {
		User user = userRepository.getUserByUserName(principal.getName());
		Contact contact = contactRepository.getReferenceById(cId);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			return "normal/update_form";
		} else {
			return "/error";
		}

	}

	@PostMapping("/update-proceess")
	public String updateProcess(@ModelAttribute("contact") Contact contact, Principal principal,
			@RequestParam("profileImage") MultipartFile multipartFile, HttpSession session) {
		System.out.println(contact);
		User user = userRepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		String oldImage = contactRepository.getReferenceById(contact.getcId()).getImage();
		if (multipartFile.isEmpty()) {
			contact.setImage(oldImage);
		} 
		else {
			String updatedImage = multipartFile.getOriginalFilename();
			try {
				File file = new ClassPathResource("/static/images").getFile();
				File deleteFile = new File(file, oldImage);
				deleteFile.delete();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("message", new Message("Contact Updated successfully!!", "success"));
				contact.setImage(updatedImage);
				contactRepository.save(contact);

			} 
			catch (Exception e) {
				session.setAttribute("message", new Message("Contact could not be  Updated!!", "danger"));
			}
			
		}

		return "redirect:/user/contact/" + contact.getcId();

	}
	
	
	@GetMapping("/profile")
	
	public String profile(Model model) {
		model.addAttribute("title", "User Profile");
		return "normal/profile";
	}
	
	
	@GetMapping("/profile-setting")
	public String profilePage(Model model,Principal principal) {
		model.addAttribute("title", "Setting");
		
		return "normal/profile_setting_page";
	}
	
	@PostMapping("/profile-setting-process")
	public String profileSettingProcess(@ModelAttribute("user") @Valid User user,BindingResult bindingResult ,Model model, Principal principal)
	{
		User oldUserDetial = userRepository.getUserByUserName(principal.getName());
		System.out.println(oldUserDetial);
		int errorCountOfPassword = bindingResult.getFieldErrorCount("password");
		
		
		if(bindingResult.hasFieldErrors("name") || errorCountOfPassword==1) {
			model.addAttribute("user", user);
			return "normal/profile_setting_page";		
		}
		
		
		oldUserDetial.setName(user.getName());
		oldUserDetial.setAbout(user.getAbout());
		if(!user.getPassword().isEmpty())
			oldUserDetial.setPassword(user.getPassword());
		
		userRepository.save(oldUserDetial);
		
		return "redirect:/user/profile";
		
	}

}
