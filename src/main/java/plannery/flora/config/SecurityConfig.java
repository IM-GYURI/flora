package plannery.flora.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import plannery.flora.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .httpBasic(AbstractHttpConfigurer::disable)

        .sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(authHttpRequest -> authHttpRequest
            .requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
            .permitAll()

            .requestMatchers("/health").permitAll()

            .requestMatchers("/members/signup", "/members/signup/admin", "/members/password")
            .permitAll()

            .requestMatchers(HttpMethod.GET, "/members/{memberId}").hasRole("MEMBER")

            .requestMatchers(HttpMethod.DELETE, "/members/{memberId}").hasAnyRole("MEMBER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/members/{memberId}/password")
            .hasAnyRole("MEMBER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/members/{memberId}/signout")
            .hasAnyRole("MEMBER", "ADMIN")

            .requestMatchers("/notifications/subscribe", "/notifications/{memberId}")
            .hasRole("MEMBER")

            .requestMatchers("/members/**").hasRole("MEMBER")

            .requestMatchers("/notifications/**").hasRole("ADMIN")

            .anyRequest().authenticated())

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    // 모든 출처 허용
    configuration.setAllowedOrigins(List.of("*"));

    // 허용할 메서드 설정
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

    // 모든 헤더 허용
    configuration.setAllowedHeaders(List.of("*"));

    // 서버가 보내는 헤더 허용
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
