package vn.io.oldmoon.shopizer.user.app.api;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/users")
@RequiredArgsConstructor
public class TestController {
    private final Tracer tracer;

    @NewSpan
    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }
}
