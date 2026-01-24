package com.creditapp.controller;

import com.creditapp.dto.ChildDTO;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("role", role);
        return "dashboard";
    }

    @GetMapping("/parent/children")
    public String parentChildren(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long parentId = Long.parseLong(userDetails.getUsername());
        List<ChildDTO> children = userService.getChildrenByParentId(parentId);
        model.addAttribute("children", children);
        return "parent/children";
    }

    @GetMapping("/parent/tasks")
    public String parentTasks(Model model) {
        return "parent/tasks";
    }

    @GetMapping("/parent/rewards")
    public String parentRewards(Model model) {
        return "parent/rewards";
    }

    @GetMapping("/child/tasks")
    public String childTasks(Model model) {
        return "child/tasks";
    }

    @GetMapping("/child/rewards")
    public String childRewards(Model model) {
        return "child/rewards";
    }

    @GetMapping("/child/create-child")
    public String showCreateChild() {
        return "common/create-child";
    }

    @PostMapping("/parent/create-child")
    public String createChild(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String username,
                              @RequestParam String password,
                              Model model) {
        try {
            Long parentId = Long.parseLong(userDetails.getUsername());
            userService.createChild(parentId, username, password);
            return "redirect:/parent/children?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "common/create-child";
        }
    }
}
