package org.kiru.chat.application.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketUserService {
    private final ConcurrentMap<String, Boolean> userConnectionStatus = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TranslateLanguage> userTranslateStatus = new ConcurrentHashMap<>();

    /**
     * Checks if a user is currently connected to the system.
     *
     * @param userId the unique identifier of the user to check
     * @return true if the user is connected, false otherwise
     */
    public boolean isUserConnected(String userId) {
        return userConnectionStatus.getOrDefault(userId, false);
    }

    /**
     * Updates the connection status for a specific user in the system.
     *
     * @param userId The unique identifier of the user whose connection status is being updated
     * @param isConnected A boolean flag indicating whether the user is connecting (true) or disconnecting (false)
     *
     * When a user connects:
     * - The user's ID is added to the userConnectionStatus map with a value of true
     *
     * When a user disconnects:
     * - The user's ID is removed from the userConnectionStatus map
     * - The user's translation preference is also removed from the userTranslateStatus map
     */
    public void updateUserConnectionStatus(final String userId, final boolean isConnected) {
        if (isConnected) {
            userConnectionStatus.put(userId, true);
        }else{
            userConnectionStatus.remove(userId);
            userTranslateStatus.remove(userId);
        }
    }

    /**
     * Retrieves a list of user IDs for currently connected users.
     *
     * @return A list of user IDs (as Long values) who are currently connected to the system.
     * @see ConcurrentMap
     */
    public List<Long> getConnectedUserIds() {
        return userConnectionStatus.entrySet().stream()
                .filter(Entry::getValue)
                .map(entry -> Long.parseLong(entry.getKey()))
                .toList();
    }

    /**
     * Updates the translation preference for a specific user.
     *
     * @param userId The unique identifier of the user whose translation preference is being set
     * @param targetLanguage The language code representing the user's desired translation language
     * @throws IllegalArgumentException If the provided target language is not a valid {@code TranslateLanguage}
     */
    public void updateUserTranslationPreference(final String userId,final String targetLanguage) {
        userTranslateStatus.put(userId, TranslateLanguage.valueOf(targetLanguage));
    }

    /**
     * Checks if a user is connected and has a translation preference set.
     *
     * @param userId The unique identifier of the user to check
     * @return The user's translation language if connected and translation preference exists, otherwise null
     * @throws NullPointerException if userId is null
     */
    public TranslateLanguage isUserConnectedAndTranslate(final String userId) {
        requireNonNull(userId, "User ID must be provided");
        if(Boolean.TRUE.equals(userConnectionStatus.getOrDefault(userId, false)) && userTranslateStatus.containsKey(userId)){
            return userTranslateStatus.get(userId);
        }
        return null;
    }
}