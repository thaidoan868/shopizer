package vn.io.oldmoon.shopizer.user.app.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Header;

import java.io.IOException;

@Component
public class AddCorrelationIdFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(Header.CORRELATION_ID);

        if (correlationId != null && !correlationId.isBlank()) {
            response.setHeader(Header.CORRELATION_ID, correlationId);
        }

        filterChain.doFilter(request, response);
    }
}
