package com.example.creditinput.controller;

import com.example.creditinput.model.CreditApplicationRequest;
import com.example.creditinput.model.CreditApplicationResponse;
import com.example.creditinput.model.PolicyResponse;
import com.example.creditinput.service.CreditStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/credit")
@CrossOrigin(origins = "http://localhost:8080")
public class CreditController {

    private static final Logger log = LoggerFactory.getLogger(CreditController.class);
    private final CreditStorageService storageService;
    private final RestTemplate restTemplate;
    private final String policyGeneratorGenerateUrl;

    public CreditController(
            CreditStorageService storageService,
            RestTemplate restTemplate,
            @Value("${app.policy-generator-generate-url}") String policyGeneratorGenerateUrl
    ) {
        this.storageService = storageService;
        this.restTemplate = restTemplate;
        this.policyGeneratorGenerateUrl = policyGeneratorGenerateUrl;
    }

    @PostMapping("/apply")
    public ResponseEntity<CreditApplicationResponse> apply(@RequestBody CreditApplicationRequest request) {
        validate(request);
        storageService.save(request);
        log.info("Received credit application: {}", request);

        PolicyResponse policyResponse;
        try {
            log.info("Calling policy-generator via {}", policyGeneratorGenerateUrl);
            policyResponse = restTemplate.postForObject(
                    policyGeneratorGenerateUrl,
                    request,
                    PolicyResponse.class
            );
            if (policyResponse != null) {
                log.info("Received policy-generator response with application number {}", policyResponse.getApplicationNumber());
            }
        } catch (Exception ex) {
            log.error("Failed to call policy-generator service", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to contact policy-generator service.");
        }

        if (policyResponse == null || policyResponse.getApplicationNumber() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Policy-generator service returned invalid response.");
        }

        return ResponseEntity.ok(new CreditApplicationResponse(policyResponse.getApplicationNumber()));
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
