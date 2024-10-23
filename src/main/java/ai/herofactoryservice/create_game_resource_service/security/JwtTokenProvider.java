//package ai.herofactoryservice.create_game_resource_service.security;
//
//import ai.herofactoryservice.create_game_resource_service.model.Member;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Value("${jwt.expiration}")
//    private long validityInMilliseconds;
//
//    private Key key;
//
//    @PostConstruct
//    protected void init() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        key = Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public String createToken(Member member) {
//        Claims claims = Jwts.claims().setSubject(member.getEmail());
//        claims.put("id", member.getId());
//        claims.put("role", "USER");
//
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + validityInMilliseconds);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public Authentication getAuthentication(String token) {
//        Claims claims = parseClaims(token);
//        Collection<? extends GrantedAuthority> authorities =
//                Arrays.stream(new String[]{"ROLE_USER"})
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());
//
//        UserDetails principal = new User(claims.getSubject(), "", authorities);
//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    private Claims parseClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}