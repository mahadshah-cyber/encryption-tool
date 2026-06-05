package com.encryptool.model;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class AESUtil {
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public static void generateAndSaveKey(String path) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        Files.write(Path.of(path), kg.generateKey().getEncoded());
    }

    public static byte[] encrypt(byte[] plaintext, byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[IV_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE, iv));
        return ByteBuffer.allocate(IV_SIZE + c.doFinal(plaintext).length).put(iv).put(c.doFinal(plaintext)).array();
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        ByteBuffer bb = ByteBuffer.wrap(ciphertext);
        byte[] iv = new byte[IV_SIZE]; bb.get(iv);
        byte[] ct = new byte[bb.remaining()]; bb.get(ct);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE, iv));
        return c.doFinal(ct);
    }
}
