package com.example.policygenerator.controller;

import com.example.policygenerator.model.CreditApplicationRequest;
import com.example.policygenerator.model.PolicyResponse;
import com.example.policygenerator.service.PolicyNumberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private static final Logger log = LoggerFactory.getLogger(PolicyController.class);
    private final PolicyNumberService policyNumberService;

    public PolicyController(PolicyNumberService policyNumberService) {
        this.policyNumberService = policyNumberService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PolicyResponse> generate(@RequestBody CreditApplicationRequest request) {
        validate(request);
        log.info("Received policy generation request: {}", request);
        String applicationNumber = policyNumberService.getOrCreateApplicationNumber(request);
        log.info("Returning policy generation response with application number {}", applicationNumber);
        return ResponseEntity.ok(new PolicyResponse(applicationNumber));
    }

    private void validate(CreditApplicationRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("\\d{10}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number must be 10 digits.");
        }
        if (request.getAadharNumber() == null || !request.getAadharNumber().matches("\\d{12}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aadhar number must be 12 digits.");
        }
        if (request.getPanNumber() == null || !request.getPanNumber().matches("[A-Za-z0-9]{10}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PAN number must be 10 alphanumeric characters.");
        }
        if (request.getCreditCardNumber() == null || request.getCreditCardNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card number is required.");
        }
        if (request.getCreditCardExpiry() == null || request.getCreditCardExpiry().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card expiry is required.");
        }
        if (request.getCvc() == null || !request.getCvc().matches("\\d{3,4}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CVC must be 3 or 4 digits.");
        }
    }
}
