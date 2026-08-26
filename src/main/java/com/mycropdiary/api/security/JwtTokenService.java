package com.mycropdiary.api.security;

import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    public String createAccessToken(CurrentUser user) {
        throw new UnsupportedOperationException("TODO Week 3: implement JWT signing with configured secret");
    }

    public CurrentUser parseAccessToken(String token) {
        throw new UnsupportedOperationException("TODO Week 3: validate JWT and map claims");
    }
}
