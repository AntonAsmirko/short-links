package anton.asmirko.app.repository.shortenedlinks;

import java.time.LocalDateTime;

public record LinkDao(
    Integer id,
    String uuid,
    String original,
    Integer numOfQueries,
    Integer queriesLimit,
    LocalDateTime expiresAt,
    LocalDateTime createdAt) {}
