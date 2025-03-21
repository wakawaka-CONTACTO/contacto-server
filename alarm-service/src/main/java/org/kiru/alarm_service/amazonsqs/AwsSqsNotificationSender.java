package org.kiru.alarm_service.amazonsqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kiru.alarm_service.amazonsqs.dto.PushMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
public class AwsSqsNotificationSender {

    private final SqsAsyncClient sqsAsyncClient;

    @Value("${cloud.aws.sqs.queue-url}")
    private String queueUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendPushMessage(PushMessage message) {
        try {
            String body = objectMapper.writeValueAsString(message);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build();

            sqsAsyncClient.sendMessage(request)
                    .thenAccept(response -> {
                        System.out.println("Message sent! ID: " + response.messageId());
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

}
