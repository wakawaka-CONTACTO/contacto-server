package org.kiru.core.chat.message.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.chat.common.BaseTimeEntity;
import org.kiru.core.chat.message.domain.TranslateLanguage;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "translate_messages")
@Getter
public class TranslateMessageJpaEntity extends BaseTimeEntity{
    @Id
    private Long messageId;

    @Enumerated(EnumType.STRING)
    private TranslateLanguage translateLanguage;

    private String message;
}
