package vn.io.oldmoon.shopizer.user.app.api;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final Tracer tracer;

    @NewSpan
    @GetMapping("/")
    public String index() {
        log.info("Starting index");
        return "Hello World!";
    }
}