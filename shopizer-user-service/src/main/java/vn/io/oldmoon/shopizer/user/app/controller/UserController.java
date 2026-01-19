package vn.io.oldmoon.shopizer.user.app.controller;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final Tracer tracer;

    @NewSpan
    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Only admin has access to this endpoint";
    }

    @GetMapping("/detail")
    public String detail() {
        return "Only authenticated users have access to this endpoint";
    }
}