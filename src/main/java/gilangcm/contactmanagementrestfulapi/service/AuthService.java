package gilangcm.contactmanagementrestfulapi.service;

import gilangcm.contactmanagementrestfulapi.entity.User;
import gilangcm.contactmanagementrestfulapi.model.LoginUserRequest;
import gilangcm.contactmanagementrestfulapi.model.TokenResponse;
import gilangcm.contactmanagementrestfulapi.repository.UserRepository;
import gilangcm.contactmanagementrestfulapi.security.Bcrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public TokenResponse login(LoginUserRequest request){
        validationService.validate(request);

        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is wrong"));

        if (Bcrypt.checkpw(request.getPassword(), user.getPassword())) {
            // SUKSES

            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiredAt(next30Days());
            userRepository.save(user);

            return TokenResponse.builder()
                    .token(user.getToken())
                    .expiredAt(user.getTokenExpiredAt())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is wrong");
        }
    }

    private Long next30Days(){
        return System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30) ;
    }

    @Transactional
    public void logout(User user) {
        user.setToken(null);
        user.setTokenExpiredAt(null);

        userRepository.save(user);
    }
}
