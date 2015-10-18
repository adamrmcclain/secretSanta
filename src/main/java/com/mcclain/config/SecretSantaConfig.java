package com.mcclain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class SecretSantaConfig {
    @Value("${secretSantaFilePath}")
    private String filePath;
}
