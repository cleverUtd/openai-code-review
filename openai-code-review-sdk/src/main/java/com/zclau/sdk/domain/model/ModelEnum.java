package com.zclau.sdk.domain.model;

import lombok.Getter;

@Getter
public enum ModelEnum {
    Qwen3_32B("Qwen/Qwen3-32B","适用于复杂的对话交互和深度内容创作设计的场景")
    ;

    private final String code;
    private final String info;

    ModelEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
