package com.cdc.ots_auth_service.security;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;

@Service
public class KmsService {

    private final KeyManagementServiceClient kmsClient;
    private final CryptoKeyName keyName;

    public KmsService(@Value("${spring.cloud.gcp.project-id}") String projectId, @Value("${gcp.kms.location}") String location, @Value("${gcp.kms.keyring}") String keyRing, @Value("${gcp.kms.key}") String key) throws IOException {
        this.kmsClient = KeyManagementServiceClient.create();
        this.keyName = CryptoKeyName.of(projectId, location, keyRing, key);
    }

    public String encrypt(String plaintext) {
        EncryptResponse response = kmsClient.encrypt(keyName, ByteString.copyFromUtf8(plaintext));
        return Base64.getEncoder().encodeToString(response.getCiphertext().toByteArray());
    }

    public String decrypt(String ciphertext) {
        ByteString decoded = ByteString.copyFrom(Base64.getDecoder().decode(ciphertext));
        DecryptResponse response = kmsClient.decrypt(keyName, decoded);
        return response.getPlaintext().toStringUtf8();
    }
    
}
