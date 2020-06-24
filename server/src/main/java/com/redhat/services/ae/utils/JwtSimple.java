package com.redhat.services.ae.utils;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.redhat.services.ae.MapBuilder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtSimple{
	public static String SECRET_KEY="THIS IS MY SECRET";
	
	public static String createJWT(String id, String issuer, String subject, long ttlMillis) {
	  
    //The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    //We will sign our JWT with our ApiKey secret
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    //Let's set the JWT Claims
    JwtBuilder builder = Jwts.builder().setId(id)
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .signWith(signatureAlgorithm, signingKey);
    
    builder.addClaims(new MapBuilder<String,Object>().put("testClaim","myAdmin").build());
    
    //if it has been specified, let's add the expiration
    if (ttlMillis > 0) {
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
    }  
  
    //Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
	}
	
	public static Claims decodeJWT(String jwt) {
    //This line will throw an exception if it is not a signed JWS (as expected)
    Claims claims = Jwts.parser()
            .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
            .parseClaimsJws(jwt).getBody();
    return claims;
}
	
}
