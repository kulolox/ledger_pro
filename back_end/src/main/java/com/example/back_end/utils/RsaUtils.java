package com.example.back_end.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.springframework.stereotype.Component;

@Component
public class RsaUtils {

    private static final Path KEY_DIR = Path.of("data");
    private static final Path PRIVATE_KEY_FILE = KEY_DIR.resolve("rsa_private.key");
    private static final Path PUBLIC_KEY_FILE = KEY_DIR.resolve("rsa_public.key");

    private final KeyPair keyPair;
    private final OAEPParameterSpec oaepSpec;

    public RsaUtils() {
        try {
            this.oaepSpec =
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            this.keyPair = loadOrGenerateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA key pair", e);
        }
    }

    private KeyPair loadOrGenerateKeyPair() throws Exception {
        if (Files.exists(PRIVATE_KEY_FILE) && Files.exists(PUBLIC_KEY_FILE)) {
            byte[] privateBytes = Files.readAllBytes(PRIVATE_KEY_FILE);
            byte[] publicBytes = Files.readAllBytes(PUBLIC_KEY_FILE);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
            return new KeyPair(publicKey, privateKey);
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair kp = generator.generateKeyPair();
        Files.createDirectories(KEY_DIR);
        Files.write(PRIVATE_KEY_FILE, kp.getPrivate().getEncoded());
        Files.write(PUBLIC_KEY_FILE, kp.getPublic().getEncoded());
        return kp;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaepSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt password: " + e.toString(), e);
        }
    }
}
