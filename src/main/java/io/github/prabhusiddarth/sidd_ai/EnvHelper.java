package io.github.prabhusiddarth.sidd_ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvHelper {

    private static final Map<String, String> dotEnvMap = new HashMap<>();

    static {
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        
                        // Strip surrounding double quotes
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        // Strip surrounding single quotes
                        else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        dotEnvMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                // Ignore silently and fall back to System.getenv()
            }
        }
    }

    /**
     * Retrieve the environment variable value. 
     * System environment variables take priority, falling back to local .env.
     */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotEnvMap.get(key);
        }
        return value;
    }
}
