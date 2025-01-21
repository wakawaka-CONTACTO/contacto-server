package org.kiru.chat.application.port.in;

import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.domain.TranslateLanguage;

public interface SendMessageUseCase {
    /**
 * Sends a message to a specified chat room with optional translation and connection status.
 *
 * @param roomId The unique identifier of the chat room where the message will be sent
 * @param message The message object to be sent
 * @param isUserConnected A flag indicating whether the user is currently connected
 * @param translateLanguage The target language for message translation
 * @return The processed message after sending, potentially translated
 */
Message sendMessage(Long roomId, Message message, boolean isUserConnected, TranslateLanguage translateLanguage);
}
