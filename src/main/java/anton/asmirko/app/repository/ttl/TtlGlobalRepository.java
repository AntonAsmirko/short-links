package anton.asmirko.app.repository.ttl;

import com.example.jooq.generated.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TtlGlobalRepository {

  private final DSLContext dslContext;

  public void setTtl(Integer ttl) {
    dslContext
        .update(Tables.TTL_GLOBAL)
        .set(Tables.TTL_GLOBAL.TTL, ttl)
        .where(Tables.TTL_GLOBAL.ID.eq(1))
        .execute();
  }

  public Integer getTtl() {
    return dslContext
        .select(Tables.TTL_GLOBAL.TTL)
        .from(Tables.TTL_GLOBAL)
        .where(Tables.TTL_GLOBAL.ID.eq(1))
        .fetchOneInto(Integer.class);
  }
}
