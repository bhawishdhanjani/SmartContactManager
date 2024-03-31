package com.smart.controller;




import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class MyController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/base")
	public String base() {
		return "base";
	}
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		
		
		model.addAttribute("title", "Register - Smart Contact Manager");
		return "signup";
	}
	@PostMapping("/do_register")
	public String do_register(@Valid @ModelAttribute("user") User user , BindingResult bindingResult , @RequestParam(value = "agreement",defaultValue = "false") boolean agreement , Model model , HttpSession session){
		
		
		try {
			if(!agreement) {
				throw new Exception("You have not agreed the terms and conditions");
			}
			if(bindingResult.hasErrors()) {
				
				model.addAttribute("user", user);
				return "signup";
			}
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setImageUrl("default.jpg");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			User result = userRepository.save(user);
			
			session.setAttribute("message", new Message("Registeration Successfull ","alert-success"));
			model.addAttribute("user", new User());
			return "signup";
			
		} catch (Exception e) {
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "signup";
			
		}
		
		
		
	}
	@GetMapping("/signin")
	public String sigin(Model model) {
	
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}
	
}
