package com.example.kmascope.controller;

import com.example.kmascope.domain.Message;
import com.example.kmascope.domain.User;
import com.example.kmascope.domain.dto.MessageDto;
import com.example.kmascope.repos.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {

    @Autowired
    private MessageRepo messageRepo;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String greeting(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("greeting", user);
        return "greeting";
    }

    @GetMapping("/main")
    public String main(
            @RequestParam(required = false, defaultValue = "") String tag,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<MessageDto> page;

        if (tag != null && !tag.isEmpty()) {
            page = messageRepo.findByTag(tag, pageable, user);
        } else {
            page = messageRepo.findAll(pageable, user);
        }

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("tag", tag);

        return "main";
    }

    @PostMapping("/main")
    public String add(@AuthenticationPrincipal User user,
                      @Valid Message message,
                      BindingResult bindingResult,
                      Model model,
                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                      @RequestParam("file") MultipartFile file
    ) throws IOException {

        Page<MessageDto> page;

        message.setAuthor(user);

        if (bindingResult.hasErrors()) {
            model.mergeAttributes(ControllerUtils.getErrors(bindingResult));
            model.addAttribute("message", message);
        } else {
            saveFile(message, model, file);

            model.addAttribute("message", null);

            //messageRepo.save(message);
        }

        page = messageRepo.findAll(pageable, user);

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        return "main";
    }

    private void saveFile(@Valid Message message,
                          Model model,
                          @RequestParam("file") MultipartFile file)
            throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + resultFilename));
            message.setFilename(resultFilename);
        }
        model.addAttribute("message", null);
        messageRepo.save(message);
    }

    @GetMapping("/user-messages/{user}")
    public String userMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User user,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Message message
    ) {
        Page<MessageDto> page = messageRepo.findByUser(pageable, user, currentUser);

        model.addAttribute("userChannel", user);
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));

        model.addAttribute("page", page);
        model.addAttribute("url", "/user-messages/" + user.getId());
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }

    @RequestMapping(value = {"user-messages/del/user-messages/{user}", "del/user-messages/{user}"},
            method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteMessage(
            @PathVariable Long user,
            @RequestParam("message") Integer messageId
    ) {
        if (messageId != null) {
            Message message1 = messageRepo.findById(messageId).get();
            messageRepo.delete(message1);
        }
        return "redirect:/user-messages/" + user;
    }

    @PostMapping("/user-messages/{user}")
    public String updateMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long user,
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            Model model,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (message != null) {
            if (message.getAuthor().equals(currentUser)) {
                checkAndSaveMessage(message, text, tag, model, file);
            }
        } else {
            Message message1 = new Message(text, tag, currentUser);
            checkAndSaveMessage(message1, text, tag, model, file);
        }
        return "redirect:/user-messages/" + user;
    }

    private void checkAndSaveMessage(
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag, Model model,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (!StringUtils.isEmpty(text)) {
            message.setText(text);
        }

        if (!StringUtils.isEmpty(tag)) {
            message.setTag(tag);
        }

        saveFile(message, model, file);

        messageRepo.save(message);
    }

    @GetMapping("/messages/{message}/like")
    public String like(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Message message,
            RedirectAttributes redirectAttributes,
            @RequestHeader(required = false) String referer
    ) {
        Set<User> likes = message.getLikes();

        if (likes.contains(currentUser)) {
            likes.remove(currentUser);
        } else {
            likes.add(currentUser);
        }

        messageRepo.save(message);

        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();

        components.getQueryParams()
                .entrySet()
                .forEach(pair -> redirectAttributes.addAttribute(pair.getKey(), pair.getValue()));

        return "redirect:" + components.getPath();
    }
}