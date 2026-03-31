package com.creditapp.service;

import lombok.*;

/**
 * 兑换结果
 * 用于封装具体礼物类型兑换后的详细信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemResult {
    
    /**
     * 是否兑换成功
     */
    private boolean success;
    
    /**
     * 兑换详情说明
     * 例如：上网券的券码信息、有效期等
     * 将填充到 RewardRedemption.note 字段
     */
    private String note;
    
    /**
     * 额外数据（可选）
     * 例如：券码对象、序列号等
     */
    private Object data;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
    
    // 快捷构建方法
    public static RedeemResult success(String note) {
        return RedeemResult.builder()
                .success(true)
                .note(note)
                .build();
    }
    
    public static RedeemResult success(String note, Object data) {
        return RedeemResult.builder()
                .success(true)
                .note(note)
                .data(data)
                .build();
    }
    
    public static RedeemResult failure(String errorMessage) {
        return RedeemResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
