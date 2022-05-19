package cn.wildfirechat.app;

import cn.wildfirechat.app.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@org.springframework.stereotype.Controller
public class WebController {
    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);
    @Autowired
    private Service mService;

    @RequestMapping("/")
    public String homepage() {
        return "redirect:/index.html";
    }

    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    public Object login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return mService.login(response, request.getAccount(), request.getPassword());
    }

    @PostMapping(value = "/account", produces = "application/json;charset=UTF-8")
    public Object getAccount() {
        return mService.getAccount();
    }
}
