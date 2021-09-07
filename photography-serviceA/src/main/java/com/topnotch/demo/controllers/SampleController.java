package com.topnotch.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;

import com.topnotch.demo.dtos.DocUploadResponse;
import com.topnotch.demo.models.EmployeeDetails;
import com.topnotch.demo.repositories.EmployeeDetailsRepository;
import com.topnotch.demo.services.TNPhotoDetailsService;
import com.topnotch.demo.utils.DiscoveryUtil;

import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/myapp/serviceA")
public class SampleController {

	@Autowired
	private DiscoveryUtil discUtil;

	@Autowired
	private TNPhotoDetailsService docService;

	@Autowired
	private EmployeeDetailsRepository repository;

	private String SERVICE_NAME = "tn-auth-service";

	@GetMapping("/homePage")
	public String displayHomepage(ServerHttpRequest request, Model model) {

		String authHeader = request.getHeaders().getFirst("Authorization");
		String userId = request.getHeaders().getFirst("UserId");

		if (authHeader == null || !authHeader.substring(0, 7).equals("Bearer ")) {

			return "redirect:" + discUtil.getServiceUri(SERVICE_NAME) + "/myapp/authService/signupPage";
		}

		EmployeeDetails employee = repository.findByEmail(userId);

		if (employee != null) {

			model.addAttribute("empDetails", employee);
		}
		return "home";
	}

	@PostMapping(value = "/uploadPage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String displaySpecialpage(ServerHttpRequest request, Model model,
			@RequestPart("fileToUpload") Flux<FilePart> filePart) throws InterruptedException {

		String authHeader = request.getHeaders().getFirst("Authorization");
		String userId = request.getHeaders().getFirst("UserId");

		if (authHeader == null || !authHeader.substring(0, 7).equals("Bearer ")) {

			return "redirect:" + discUtil.getServiceUri(SERVICE_NAME) + "/myapp/authService/signupPage";
		}

		System.out.println("Authorization : " + authHeader + "\nUserId : " + userId);

		System.out.println("Something is happening");

		Flux<DocUploadResponse> finalResponse = filePart.filter(file -> file != null).flatMap(file -> {

			Flux<DocUploadResponse> response = file.content().map(dataBuff -> {

				byte[] data = dataBuff.asByteBuffer().array();

				String docName = file.filename();

				int startIndex = docName.lastIndexOf('.');
				int endIndex = docName.length();

				String docType = docName.substring(startIndex, endIndex);

				return docService.uploadDocument(userId, docName, docType, data);
			});

			return response;

		});
		
		model.addAttribute("files", finalResponse.collectList() );

		return "upload";
	}
}

/*
 * Mono<TNPhoto> photoObj = photoFetcher.fetch();
 * 
 * Mono<PhotoDetails> details = photoObj.map( tnPhoto -> {
 * 
 * System.out.println( "Photo name : " + tnPhoto.getUrl() ); System.out.println(
 * "Width : " + tnPhoto.getWidth() ); System.out.println( "Height : " +
 * tnPhoto.getHeight() ); System.out.println( "Avg Color : " +
 * tnPhoto.getAvg_color() );
 * 
 * PhotoDetails photoDetails = photoService.uploadPhoto(tnPhoto, userId) ;
 * return photoDetails ; }) ;
 * 
 * model.addAttribute("photoDetails", details);
 */
