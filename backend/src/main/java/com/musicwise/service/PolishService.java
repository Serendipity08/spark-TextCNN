package com.musicwise.service;

import com.musicwise.dto.PolishRequest;
import com.musicwise.dto.PolishResponse;
import com.musicwise.model.BlessingPreset;
import com.musicwise.model.RecipientType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PolishService {
    private final SiliconFlowClient siliconFlowClient;

    public PolishService(SiliconFlowClient siliconFlowClient) {
        this.siliconFlowClient = siliconFlowClient;
    }

    public PolishResponse polish(PolishRequest request) {
        String content = safeTrim(request.getContent());
        String mode = safeTrim(request.getMode());
        RecipientType recipientType = request.getRecipientType() == null
                ? RecipientType.NONE
                : request.getRecipientType();
        BlessingPreset blessingPreset = request.getBlessingPreset() == null
                ? BlessingPreset.NONE
                : request.getBlessingPreset();

        boolean isVoiceMode = "voice".equalsIgnoreCase(mode);
        if (isVoiceMode) {
            if (content.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "语音内容不能为空");
            }
        } else {
            int filledCount = countFilled(content, recipientType, blessingPreset);
            if (filledCount < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "发送对象、祝福语、内容需至少填写两项");
            }
        }

        String combinedText = buildCombinedText(recipientType, blessingPreset, content);
        String polishedText;
        try {
            polishedText = siliconFlowClient.polish(combinedText);
        } catch (Exception ex) {
            polishedText = null;
        }
        if (polishedText == null || polishedText.isBlank()) {
            polishedText = fallbackPolish(combinedText);
        }
        return new PolishResponse(combinedText, polishedText);
    }

    private int countFilled(String content, RecipientType recipientType, BlessingPreset blessingPreset) {
        int count = 0;
        if (!content.isEmpty()) {
            count += 1;
        }
        if (recipientType != RecipientType.NONE) {
            count += 1;
        }
        if (blessingPreset != BlessingPreset.NONE) {
            count += 1;
        }
        return count;
    }

    private String buildCombinedText(RecipientType recipientType, BlessingPreset blessingPreset, String content) {
        StringBuilder builder = new StringBuilder();
        if (recipientType != RecipientType.NONE) {
            builder.append("给").append(recipientType.getLabel()).append("的祝福");
        }
        if (blessingPreset != BlessingPreset.NONE) {
            if (builder.length() > 0) {
                builder.append("：");
            }
            builder.append(blessingPreset.getLabel());
        }
        if (!content.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("。");
            }
            builder.append(content);
        }
        return builder.toString().trim();
    }

    private String fallbackPolish(String combinedText) {
        if (combinedText.isBlank()) {
            return "祝福送达。";
        }
        return "祝福送达：" + combinedText;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}


















