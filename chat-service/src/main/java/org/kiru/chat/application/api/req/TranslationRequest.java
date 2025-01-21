package org.kiru.chat.application.api.req;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslationRequest {
    private List<String> texts;
    private String tl;
    private String sl;
}