package com.example.bg.ai;

import java.io.*;
import java.util.*;

public class RoleProfileUtil {
    private static final String PROFILE_PATH = "data/role_profiles.txt";
    private static final Map<String, String> roleProfileMap = new HashMap<>();

    static {
        loadProfiles();
    }

   private static void loadProfiles() {
    try (BufferedReader reader = new BufferedReader(new FileReader(PROFILE_PATH))) {
        String line;
        String currentRole = null;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 判断是否是“角色”开头
            if (line.startsWith("角色")) {
                // 保存上一个角色
                if (currentRole != null) {
                    roleProfileMap.put(currentRole, sb.toString().trim());
                    sb.setLength(0);
                }
                // 提取角色名称，如“李白”
                int splitIndex = line.indexOf("：");
                if (splitIndex != -1) {
                    currentRole = line.substring(splitIndex + 1).trim();
                } else {
                    currentRole = line.trim(); // 备用容错
                }
            } else if (currentRole != null) {
                sb.append(line).append("\n");
            }
        }
        // 保存最后一个角色
        if (currentRole != null) {
            roleProfileMap.put(currentRole, sb.toString().trim());
        }
    } catch (Exception e) {
        System.err.println("读取角色信息失败: " + e.getMessage());
    }
}
    public static String getProfile(String role) {
        return roleProfileMap.getOrDefault(role, "");
    }

    public static Set<String> getSupportedRoles() {
        return roleProfileMap.keySet();
    }
}