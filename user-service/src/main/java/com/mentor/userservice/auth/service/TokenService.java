package com.mentor.userservice.auth.service;

import com.mentor.userservice.auth.dto.TokenResponse;
import com.mentor.userservice.config.JwtProperties;
import com.mentor.userservice.user.entity.UserAccount;
import com.nimbusds.jose.jwk.RSAKey;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final RSAKey rsaKey; // <-- Injected automatically from your JwtConfig!

    public TokenResponse issue(UserAccount user) {
        Instant now = Instant.now();

        // Keep the token small and free of personal data: id and role are enough
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenTtl()))
                .claim("roles", List.of(user.getRole().name()))
                .build();

        // Uses the dynamic keyID generated in your JwtConfig
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(rsaKey.getKeyID())
                .build();

        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResponse(token, "Bearer", properties.accessTokenTtl().toSeconds());
    }
}