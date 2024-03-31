package com.smart.helper;

import java.net.http.HttpRequest;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {
	public void removeMessageFromSession() {
		try {
		HttpSession httpSession = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
		httpSession.removeAttribute("message");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
	

}
