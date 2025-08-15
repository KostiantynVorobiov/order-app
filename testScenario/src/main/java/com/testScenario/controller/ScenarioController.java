package com.testScenario.controller;

import com.testScenario.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ScenarioController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping("/add-clients")
    public ResponseEntity<String> generateClientData() {
        logger.info("Generate random client data to DB");
        String response = scenarioService.createClientsData();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenario-1")
    public ResponseEntity<String> runScenarioOne() {
        logger.info("Scenario 1: Trying to create 10 identical orders");
        String response = scenarioService.executeScenarioOne();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenario-2")
    public ResponseEntity<String> runScenarioTwo() {
        logger.info("Scenario 2: Trying to create 10 identical orders with decreasing prices");
        String response = scenarioService.runScenarioTwo();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenario-3")
    public ResponseEntity<String> runScenarioThree() throws InterruptedException {
        logger.info("Scenario 3: Creating multiple orders while deactivating a client.");
        String response = scenarioService.runScenarioThree();
        return ResponseEntity.ok(response);
    }
}
