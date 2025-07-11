package com.microservice.auth;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        // Genera una clave secreta aleatoria y segura para HS256
        byte[] secretKeyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();

        // Codifica la clave en Base64 para usarla en application.properties
        String base64Secret = Base64.getEncoder().encodeToString(secretKeyBytes);

        System.out.println("Tu clave secreta JWT generada (Base64):");
        System.out.println(base64Secret);
    }
}