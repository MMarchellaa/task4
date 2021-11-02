package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController
public class SocialApplication extends WebSecurityConfigurerAdapter {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private SessionRegistry sessionRegistry;

    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
    }

    public User getLastBroUser() {
        ArrayList<User> users = userRepository.getUsersByLastTimeClickedBroButtonIsNotNull().orElseGet(ArrayList::new);
        return users.stream().max(Comparator.comparing(User::getLastTimeClickedBroButton)).orElseGet(User::new);
    }

    public User getLastSisUser() {
        ArrayList<User> users = userRepository.getUsersByLastTimeClickedSisButtonIsNotNull().orElseGet(ArrayList::new);
        return users.stream().max(Comparator.comparing(User::getLastTimeClickedSisButton)).orElseGet(User::new);
    }

    @GetMapping("/lastSis")
    public Map<String, String> getLastSis() {
        User user = getLastSisUser();
        return Collections.singletonMap("lastsis", "Sent by " + user.getName() + " at " + user.getLastTimeClickedSisButton().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @GetMapping("/lastBro")
    public Map<String, String> getLastBro() {
        User user = getLastBroUser();
        return Collections.singletonMap("lastbro", "Sent by " + user.getName() + " at " + user.getLastTimeClickedBroButton().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @GetMapping("/countSisUsers")
    public Map<String, Integer> countSisUsers() {
        ArrayList<User> sisUsers = userRepository.getUsersByLastTimeClickedSisButtonIsNotNull().orElseGet(ArrayList::new);
        return Collections.singletonMap("countSisUsers", sisUsers.size());
    }

    @GetMapping("/countBroUsers")
    public Map<String, Integer> countBroUsers() {
        ArrayList<User> broUsers = userRepository.getUsersByLastTimeClickedBroButtonIsNotNull().orElseGet(ArrayList::new);
        return Collections.singletonMap("countBroUsers", broUsers.size());
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getAttribute("name"));
    }

    @PostMapping("/logout")
    public @ResponseBody
    String logout(@AuthenticationPrincipal OAuth2User principal) {
        User user = new User(principal.getName());
        List<SessionInformation> infos = sessionRegistry.getAllSessions(user, false);
        for (SessionInformation info : infos) {
            info.expireNow();
            sessionRegistry.removeSessionInformation(info.getSessionId());
        }
        return "ok";
    }

    @GetMapping("/isAuthenticated")
    public Map<String, Boolean> isAuthenticated(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("isAuthenticated", principal != null);
    }

    @PostMapping("/brobutton")
    public void broButtonClick(@AuthenticationPrincipal OAuth2User principal) {
        User user = new User();
        user.setName(principal.getAttribute("name"));
        user.setLastTimeClickedBroButton(LocalDateTime.now());
        userRepository.save(user);
    }

    @PostMapping("/sisbutton")
    public void sisButtonClick(@AuthenticationPrincipal OAuth2User principal) {
        User user = new User();
        user.setName(principal.getAttribute("name"));
        user.setLastTimeClickedSisButton(LocalDateTime.now());
        userRepository.save(user);
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(a -> a
                        .antMatchers("/", "/error", "/countSisUsers", "/countBroUsers", "/isAuthenticated", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutSuccessUrl("/").permitAll()
                )
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login();
    }

}