package com.manideep.work_tracker.controller;

import com.manideep.work_tracker.dto.WorkItemRequest;
//import com.manideep.work_tracker.service.EmailService;
import com.manideep.work_tracker.service.WorkItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.validation.BindingResult;
@Controller
public class WorkController {

    private final WorkItemService workItemService;
    //private final EmailService emailService;

    public WorkController(
            WorkItemService workItemService){
            //EmailService emailService) {

        this.workItemService = workItemService;
        //this.emailService = emailService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "pendingWorks",
                workItemService.getPendingWorks()
        );

        model.addAttribute(
                "completedWorks",
                workItemService.getCompletedWorks()
        );

        model.addAttribute(
                "workItemRequest",
                new WorkItemRequest()
        );

        return "index";
    }

    @PostMapping("/works")
    public String addWork(
            @Valid @ModelAttribute WorkItemRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pendingWorks",
                    workItemService.getPendingWorks()
            );

            model.addAttribute(
                    "completedWorks",
                    workItemService.getCompletedWorks()
            );

            return "index";
        }

        workItemService.addWork(
                request.getTitle(),
                request.getDescription()
        );

        return "redirect:/";
    }

    @PostMapping("/works/{id}/complete")
    public String completeWork(
            @PathVariable Long id) {

        workItemService.completeWork(id);

        return "redirect:/";
    }

    @PostMapping("/works/{id}/delete")
    public String deleteWork(
            @PathVariable Long id) {

        workItemService.deleteWork(id);

        return "redirect:/";
    }

//    @GetMapping("/test-email/morning")
//    @ResponseBody
//    public String testMorningEmail() {
//        emailService.sendMorningSummary();
//        return "Morning test email sent successfully";
//    }
//
//    @GetMapping("/test-email/midnight")
//    @ResponseBody
//    public String testMidnightEmail() {
//        emailService.sendPreviousDaySummary();
//        return "Midnight test email sent successfully";
//    }
}