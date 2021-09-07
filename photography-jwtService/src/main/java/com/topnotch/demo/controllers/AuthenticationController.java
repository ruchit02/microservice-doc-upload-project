package com.topnotch.demo.controllers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.topnotch.demo.dtos.EmployeeDetailsDTO;
import com.topnotch.demo.dtos.LoginForm;
import com.topnotch.demo.dtos.SignUpForm;
import com.topnotch.demo.services.RegisterNewUser;
import com.topnotch.demo.utils.DiscoveryUtil;
import com.topnotch.demo.utils.JWTUtil;

@Controller
@RequestMapping("/myapp/authService")
public class AuthenticationController {

	@Autowired
	private DiscoveryUtil discUtil;

	@Autowired
	private RegisterNewUser registry;

	@Autowired
	private JWTUtil jwtUtil;

	@Autowired
	private StreamBridge bridge;

	private String SERVICE_NAME = "tn-gateway";

	@GetMapping("/signupPage")
	public String getSignUpForm(Model model) {

		model.addAttribute("signupForm", new SignUpForm());
		return "signup";
	}

	@PostMapping("/signupPage")
	public String registerUser(@ModelAttribute @Valid SignUpForm signupForm) {

		registry.register(signupForm);

		EmployeeDetailsDTO employee = new EmployeeDetailsDTO();
		employee.setFirst_name(signupForm.getFirst_name());
		employee.setLast_name(signupForm.getLast_name());
		employee.setDepartment( signupForm.getDepartment() );
		employee.setExpertise( signupForm.getExpertise() );
		employee.setEmail(signupForm.getEmail());

		System.out.println("Employee FirstName ...." + employee.getFirst_name());
		System.out.println("Employee LastName ...." + employee.getLast_name());
		System.out.println("Employee Department ...." + employee.getDepartment());
		System.out.println("Employee Expertise ...." + employee.getExpertise());
		System.out.println("Employee UserName ...." + employee.getEmail());

		bridge.send("detailsExchange-out-0", employee );
		System.out.println("Photographer object sent to message broker ....");

		return "redirect:/myapp/authService/loginPage";
	}

	@GetMapping("/loginPage")
	public String authenticateUser(Model model) {

		model.addAttribute("loginForm", new LoginForm());
		return "login";
	}

	@PostMapping("/authenticate")
	public String redirectUser(HttpServletResponse response, @ModelAttribute @Valid LoginForm loginForm) {

		// This method is just a STUB
		return "redirect:/myapp/authService/generateToken";
	}

	@GetMapping("/generateToken")
	public String generateToken(HttpServletResponse response) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(auth.getName());

		jwtUtil.init();
		String token = jwtUtil.generateToken(auth.getName());
		String username = jwtUtil.getUsername(token);
		System.out.println("Token decoded : " + username);
		
		Cookie cookie = new Cookie("jwtToken", token);
		cookie.setMaxAge(-1);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		response.addCookie(cookie);

		return "redirect:" + discUtil.getServiceUri(SERVICE_NAME) + "/myapp/gateway/endpoint3";
	}
}
