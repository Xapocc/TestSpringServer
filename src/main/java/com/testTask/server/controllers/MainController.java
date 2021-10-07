package com.testTask.server.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    @GetMapping(value = "/")
    public ModelAndView method() {
        return new ModelAndView("redirect:" + "/home");
    }

    @GetMapping("/home")
    public String home(Model model, String taskNumber, String userID, String levelID, String result) {

        if(taskNumber == null)
            taskNumber = "3";

        model.addAttribute("result", RequestHandler.GetResult(taskNumber, userID, levelID, result));

        return "home"; // .html
    }

}