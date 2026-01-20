package com.platform.SkyMaster_Hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/schedules")
    public String schedules() {
        return "airport-schedule";
    }
}
