package com.debojmuk.covidtracker.controller;

import com.debojmuk.covidtracker.models.LocationStats;
import com.debojmuk.covidtracker.services.CovidDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    CovidDataService covidDataService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("locationStats", covidDataService.getAllStats());
        Integer totalActive = covidDataService.getAllStats().stream().map(LocationStats::getLatestTotalCases)
                .reduce((x, y) -> x + y).get();
        model.addAttribute("totalActiveCases", totalActive.intValue());
        return "home"; // map to the html file
    }
}
