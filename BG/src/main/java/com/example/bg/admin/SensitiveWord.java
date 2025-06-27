package com.example.bg.admin;

import lombok.Data;
import java.io.Serializable;

@Data
public class SensitiveWord implements Serializable {
    private int id;
    private String word;
    private String createdTime;
    private boolean isActive;
}