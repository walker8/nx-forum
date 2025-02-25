package com.leyuz.bbs.user;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.uc.auth.token.JwtTokenProvider;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserLoginDTO;
import com.leyuz.uc.user.dto.UserResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserApplicationTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserApplication userApplication;

    private UserLoginDTO userLoginDTO;
    private UserResp expectedUserResp;

    @BeforeEach
    public void setUp() {
        userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("testUser");
        userLoginDTO.setPassword("password");

        expectedUserResp = new UserResp();
        expectedUserResp.setAccessToken("testAccessToken");
    }


    @Test
    public void whenUsernameIsNull_thenThrowValidationException() {
        userLoginDTO.setUserName(null);

        assertThrows(ValidationException.class, () -> {
            userApplication.login(userLoginDTO);
        });
    }

    // Additional test cases can be added here to cover more scenarios
}
