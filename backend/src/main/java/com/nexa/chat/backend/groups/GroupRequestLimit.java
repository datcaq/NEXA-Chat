package com.nexa.chat.backend.groups;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Bounds JSON bytes as well as logical batch sizes, including chunked requests. */
@Component
public class GroupRequestLimit extends OncePerRequestFilter {
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equals(request.getMethod())
        || !(request.getRequestURI().startsWith("/api/v1/groups")
            || request.getRequestURI().startsWith("/api/v1/group-invitations"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    byte[] body = request.getInputStream().readNBytes(65537);
    if (body.length > 65536) {
      response.setStatus(413);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              "{\"code\":\"REQUEST_TOO_LARGE\",\"message\":\"Maximum JSON body is 64"
                  + " KiB\",\"requestId\":\""
                  + UUID.randomUUID()
                  + "\"}");
      return;
    }
    chain.doFilter(
        new HttpServletRequestWrapper(request) {
          @Override
          public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
              public int read() {
                return input.read();
              }

              public int read(byte[] b, int off, int len) {
                return input.read(b, off, len);
              }

              public boolean isFinished() {
                return input.available() == 0;
              }

              public boolean isReady() {
                return true;
              }

              public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException("Synchronous MVC input");
              }
            };
          }

          @Override
          public BufferedReader getReader() {
            return new BufferedReader(
                new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
          }
        },
        response);
  }
}
