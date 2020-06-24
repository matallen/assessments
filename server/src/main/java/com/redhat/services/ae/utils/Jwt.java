package com.redhat.services.ae.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import org.eclipse.microprofile.jwt.Claims;

import com.redhat.services.ae.MapBuilder;

import io.smallrye.jwt.build.JwtClaimsBuilder;

public class Jwt {

    private Jwt() {
        // no-op: utility class
    }

    public static String createJWT(Map<String,Object> jwtClaims, long durationInSeconds) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
      return Jwt.generateTokenString(jwtClaims, 
      		durationInSeconds>1?
      				new MapBuilder<String,Long>().put(Claims.exp.name(), Jwt.currentTimeInSecs() + durationInSeconds).build():
      				new MapBuilder<String,Long>().build());
    }
    
    public static String generateTokenString(Map<String,Object> jwtClaims, Map<String, Long> timeClaims) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
      // Use the test private key associated with the test public key for a valid signature
      PrivateKey pk = readPrivateKey("/privateKey.pem");
      return generateTokenString(pk, "privateKey.pem", jwtClaims, timeClaims);
    }
    
    public static String generateTokenString(PrivateKey privateKey, String kid, Map<String,Object> jwtClaims, Map<String, Long> timeClaims)  {
    	
      JwtClaimsBuilder claims = io.smallrye.jwt.build.Jwt.claims(jwtClaims);
      long currentTimeInSecs = currentTimeInSecs();
      long exp = timeClaims != null && timeClaims.containsKey(Claims.exp.name()) ? timeClaims.get(Claims.exp.name()) : currentTimeInSecs + 300;

      claims.issuedAt(currentTimeInSecs);
      claims.claim(Claims.auth_time.name(), currentTimeInSecs);
      claims.expiresAt(exp);

      return claims.jws().signatureKeyId(kid).sign(privateKey);
    }
    
    
    /**
     * Read a PEM encoded private key from the classpath
     *
     * @param pemResName - key file resource name
     * @return PrivateKey
     * @throws IOException 
     * @throws InvalidKeySpecException 
     * @throws NoSuchAlgorithmException 
     * @throws Exception on decode failure
     */
    public static PrivateKey readPrivateKey(final String pemResName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream contentIS = Jwt.class.getResourceAsStream(pemResName)) {
            byte[] tmp = new byte[4096];
            int length = contentIS.read(tmp);
            return decodePrivateKey(new String(tmp, 0, length, "UTF-8"));
        }
    }

    /**
     * Decode a PEM encoded private key string to an RSA PrivateKey
     *
     * @param pemEncoded - PEM string for private key
     * @return PrivateKey
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws Exception on decode failure
     */
    public static PrivateKey decodePrivateKey(final String pemEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

   private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }

    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }

    /**
     * @return the current time in seconds since epoch
     */
    public static int currentTimeInSecs() {
        long currentTimeMS = System.currentTimeMillis();
        return (int) (currentTimeMS / 1000);
    }

}