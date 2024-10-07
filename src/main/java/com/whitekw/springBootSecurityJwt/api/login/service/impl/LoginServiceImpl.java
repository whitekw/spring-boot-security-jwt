package com.whitekw.springBootSecurityJwt.api.login.service.impl;

import com.whitekw.springBootSecurityJwt.api.login.dto.LoginRequestDto;
import com.whitekw.springBootSecurityJwt.api.login.dto.LoginResponseDto;
import com.whitekw.springBootSecurityJwt.api.login.service.LoginService;
import com.whitekw.springBootSecurityJwt.api.token.vo.RefreshToken;
import com.whitekw.springBootSecurityJwt.api.user.dto.UserGetResponseDto;
import com.whitekw.springBootSecurityJwt.api.user.service.UserGetService;
import com.whitekw.springBootSecurityJwt.config.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserGetService userGetService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        UserGetResponseDto userInfo = userGetService.getUserByEmail(loginRequestDto.getEmail());

        if (!bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), userInfo.password())) {
            return null;
        }

        String accessToken = jwtProvider.generateAccessToken(userInfo.userId());

        RefreshToken.removeUserRefreshToken(userInfo.userId());

        String refreshToken = jwtProvider.generateRefreshToken(userInfo.userId());
        RefreshToken.putRefreshToken(refreshToken, userInfo.userId());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
