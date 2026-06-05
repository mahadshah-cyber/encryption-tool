package com.encryptool.model;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.file.*;
import java.security.*;

public class RSAUtil {
    public static void generateAndSaveKeys(String dir, String name) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Path.of(dir, name + "_public.key")))) { oos.writeObject(kp.getPublic()); }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Path.of(dir, name + "_private.key")))) { oos.writeObject(kp.getPrivate()); }
    }
    public static PublicKey loadPublicKey(String path) throws Exception { try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Path.of(path)))) { return (PublicKey) ois.readObject(); } }
    public static PrivateKey loadPrivateKey(String path) throws Exception { try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Path.of(path)))) { return (PrivateKey) ois.readObject(); } }
    public static byte[] encrypt(byte[] data, PublicKey key) throws Exception { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, key); return c.doFinal(data); }
    public static byte[] decrypt(byte[] data, PrivateKey key) throws Exception { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.DECRYPT_MODE, key); return c.doFinal(data); }
}
