package com.topnotch.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.topnotch.demo.dtos.DocUploadResponse;
import com.topnotch.demo.dtos.EmployeeDetailsDTO;
import com.topnotch.demo.models.EmployeeDetails;
import com.topnotch.demo.models.EmployeeDocuments;
import com.topnotch.demo.repositories.EmployeeDetailsRepository;
import com.topnotch.demo.repositories.EmployeeDocumentsRepository;

@Service
public class TNPhotoDetailsService {

	@Autowired
	private EmployeeDetailsRepository employeeDetailsRepository;

	@Autowired
	private EmployeeDocumentsRepository employeeDocumentsRepository;

	public void createEmployeeAcc(EmployeeDetailsDTO employeeDetailsDTO) {

		System.out.println("Mapping credentials to database object ....");

		EmployeeDetails employee = new EmployeeDetails();

		employee.setFirst_name(employeeDetailsDTO.getFirst_name());
		employee.setLast_name(employeeDetailsDTO.getLast_name());
		employee.setDepartment(employeeDetailsDTO.getDepartment());
		employee.setExpertise(employeeDetailsDTO.getExpertise());
		employee.setEmail(employeeDetailsDTO.getEmail());

		employeeDetailsRepository.saveAndFlush(employee);
		System.out.println("Object pushed to database ....");
	}

	public DocUploadResponse uploadDocument(String email, String doc_name, String doc_type, byte[] data) {

		System.out.println("Mapping doc's metadata to doc database object ....");

		EmployeeDocuments emp_doc = new EmployeeDocuments();
		emp_doc.setDoc_name(doc_name);
		emp_doc.setDoc_type(doc_type);
		emp_doc.setData(data);

		EmployeeDetails employee = employeeDetailsRepository.findByEmail(email);
		emp_doc.setEmp_id(employee);

		System.out.println("Database object created ....");

		employeeDocumentsRepository.saveAndFlush(emp_doc);

		System.out.println("Photo object pushed to database ....");

		return new DocUploadResponse(doc_name, doc_type);
	}
}
