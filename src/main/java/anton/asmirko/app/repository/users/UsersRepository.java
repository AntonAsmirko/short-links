package anton.asmirko.app.repository.users;

import com.example.jooq.generated.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsersRepository {

  private final DSLContext dslContext;

  public Integer getUUID(final String uuid) {
    return dslContext
        .select(Tables.USERS.ID)
        .from(Tables.USERS)
        .where(Tables.USERS.UUID.eq(uuid))
        .fetchOneInto(Integer.class);
  }

  public void addUser(final String uuid) {
    dslContext.insertInto(Tables.USERS, Tables.USERS.UUID).values(uuid).execute();
  }
}
