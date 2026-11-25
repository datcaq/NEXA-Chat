package com.nexa.chat.backend.groups;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class GroupConfiguration {
  @Bean
  @ConditionalOnMissingBean
  GroupDirectory unavailableDirectory() {
    return new GroupDirectory() {
      public void requireExistingUser(UUID user) {
        throw unavailable();
      }

      public void requireEligibleInvitation(UUID a, UUID b) {
        throw unavailable();
      }

      private GroupProblem unavailable() {
        return new GroupProblem(
            503, "DIRECTORY_UNAVAILABLE", "User directory integration is not configured");
      }
    };
  }

  @Bean
  @ConditionalOnMissingBean
  JwtDecoder jwtDecoder(
      @Value("${NEXA_AUTH_ISSUER:}") String issuer,
      @Value("${NEXA_AUTH_AUDIENCE:nexa-api}") String audience) {
    if (issuer.isBlank())
      return token -> {
        throw new BadJwtException("Authentication integration is not configured");
      };
    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
    OAuth2TokenValidator<Jwt> aud =
        jwt ->
            jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid audience", null));
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), aud));
    return decoder;
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.anyRequest().authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> {})
                    .authenticationEntryPoint(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"code\":\"UNAUTHENTICATED\",\"message\":\"Authentication"
                                      + " required\",\"requestId\":\""
                                      + UUID.randomUUID()
                                      + "\"}");
                        }))
        .build();
  }
}
