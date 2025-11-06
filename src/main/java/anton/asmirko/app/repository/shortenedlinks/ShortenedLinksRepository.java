package anton.asmirko.app.repository.shortenedlinks;

import com.example.jooq.generated.Tables;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShortenedLinksRepository {

  private final DSLContext dslContext;

  public void saveShortened(
      final Integer userId,
      final Integer initialId,
      final String shortened,
      final LocalDateTime createdAt,
      final LocalDateTime expiresAt,
      final Integer numQueries,
      final Integer queriesLimit) {
    dslContext
        .insertInto(Tables.SHORTENED_LINKS)
        .set(Tables.SHORTENED_LINKS.SHORTENED, shortened)
        .set(Tables.SHORTENED_LINKS.ORIGINAL_ID, initialId)
        .set(Tables.SHORTENED_LINKS.USER_ID, userId)
        .set(Tables.SHORTENED_LINKS.CREATED_AT, createdAt)
        .set(Tables.SHORTENED_LINKS.EXPIRES_AT, expiresAt)
        .set(Tables.SHORTENED_LINKS.NUM_QUERIES, numQueries)
        .set(Tables.SHORTENED_LINKS.QUERIES_LIMIT, queriesLimit)
        .execute();
  }

  public LinkDao getLink(final String shortened) {
    return dslContext
        .select(
            Tables.SHORTENED_LINKS.ID,
            Tables.USERS.UUID,
            Tables.LINKS.ORIGINAL,
            Tables.SHORTENED_LINKS.NUM_QUERIES.as("numOfQueries"),
            Tables.SHORTENED_LINKS.QUERIES_LIMIT.as("queriesLimit"),
            Tables.SHORTENED_LINKS.EXPIRES_AT.as("expiresAt"),
            Tables.SHORTENED_LINKS.CREATED_AT.as("createdAt"))
        .from(Tables.SHORTENED_LINKS)
        .join(Tables.LINKS)
        .on(Tables.SHORTENED_LINKS.ORIGINAL_ID.eq(Tables.LINKS.ID))
        .join(Tables.USERS)
        .on(Tables.SHORTENED_LINKS.USER_ID.eq(Tables.USERS.ID))
        .where(Tables.SHORTENED_LINKS.SHORTENED.eq(shortened))
        .fetchOneInto(LinkDao.class);
  }

  public void deleteLinkById(Integer id) {
    dslContext.deleteFrom(Tables.SHORTENED_LINKS).where(Tables.SHORTENED_LINKS.ID.eq(id)).execute();
  }

  public int deleteLink(String link) {
    return dslContext
        .deleteFrom(Tables.SHORTENED_LINKS)
        .where(Tables.SHORTENED_LINKS.SHORTENED.eq(link))
        .execute();
  }

  public void updateNumQueries(Integer id, Integer newNumQueries) {
    dslContext
        .update(Tables.SHORTENED_LINKS)
        .set(Tables.SHORTENED_LINKS.NUM_QUERIES, newNumQueries)
        .where(Tables.SHORTENED_LINKS.ID.eq(id))
        .execute();
  }

  public int deleteUsersLink(Integer id, String link) {
    return dslContext
        .deleteFrom(Tables.SHORTENED_LINKS)
        .where(Tables.SHORTENED_LINKS.USER_ID.eq(id))
        .and(Tables.SHORTENED_LINKS.SHORTENED.eq(link))
        .execute();
  }

  public List<String> getUsersLinks(Integer id) {
    return dslContext
        .select(Tables.SHORTENED_LINKS.SHORTENED)
        .from(Tables.SHORTENED_LINKS)
        .where(Tables.SHORTENED_LINKS.USER_ID.eq(id))
        .fetchInto(String.class);
  }
}
