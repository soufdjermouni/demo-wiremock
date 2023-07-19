package com.prive.wiremock.controller;

import com.prive.wiremock.dto.UniversityDTO;
import com.prive.wiremock.service.TestService;
import com.prive.wiremock.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api")
public class UniversityController {

    private final UniversityService universityService;

    private final TestService testService;

    public UniversityController(UniversityService universityService, TestService testService) {
        this.universityService = universityService;
        this.testService = testService;
    }

    @GetMapping("/university")
    @Operation(summary = "Get the universities for a given country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Universities for the given country",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UniversityDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Universities not found for the given country",
                    content = @Content)})
    public List<UniversityDTO> getUniversitiesForCountry(@RequestParam String country) {
        return universityService.getUniversitiesForCountry(country);
    }

    @GetMapping("/hello")
    public String hello() {
        return   testService.hello();
    }

    @GetMapping("/greeting")
    public String greeting() {
        return   testService.greeting();
    }
}
