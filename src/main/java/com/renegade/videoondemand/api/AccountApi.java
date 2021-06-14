package com.renegade.videoondemand.api;

import com.renegade.videoondemand.domain.entity.User;
import com.renegade.videoondemand.domain.repository.UserRepository;
import com.renegade.videoondemand.service.TokenService;
import com.renegade.videoondemand.util.EtagHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountApi {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<User> getUser(@RequestHeader("sessionID") String sessionID) {
        User user = tokenService.getUserByToken(sessionID);
        return ResponseEntity.ok()
                .eTag(user.getVersion().toString())
                .body(user);
    }

    @PutMapping
    public void updateUser(@RequestHeader("sessionID") String sessionID, @RequestBody User user,
                           @RequestHeader(value = "If-Match", required=false) String ifMatch) {
        User userInDatabase = tokenService.getUserByToken(sessionID);
        EtagHelper.checkEtagCorrectness(userInDatabase.getVersion(), ifMatch);
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(userInDatabase.cloneAll(user));
    }

    @DeleteMapping
    public void deleteUser(@RequestHeader("sessionID") String sessionID) {
        User user = tokenService.getUserByToken(sessionID);
        tokenService.deleteAllUserTokens(user.getUsername());
        userRepository.deleteById(user.getUsername());
    }

    @PatchMapping
    public void patchUser(@RequestHeader("sessionID") String sessionID, @RequestBody User user,
                          @RequestHeader(value = "If-Match", required=false) String ifMatch) {
        User userInDatabase = tokenService.getUserByToken(sessionID);
        EtagHelper.checkEtagCorrectness(userInDatabase.getVersion(), ifMatch);
        user.setPassword(user.getPassword() != null ? encoder.encode(user.getPassword()) : null);
        userRepository.save(userInDatabase.cloneSome(user));
    }


}
