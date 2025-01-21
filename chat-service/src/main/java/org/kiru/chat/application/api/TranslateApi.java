package org.kiru.chat.application.api;

import org.kiru.chat.application.api.req.TranslationRequest;
import org.kiru.chat.application.api.res.TranslationResponse;
import org.kiru.chat.config.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "translate", url = "${translate.api.url}", configuration = FeignConfig.class)
public interface TranslateApi {
    @PostMapping("/translate")
    TranslationResponse translateHtml(@RequestBody TranslationRequest request);
}