package anton.asmirko.app.repository.links;

import com.example.jooq.generated.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinksRepository {

  private final DSLContext dslContext;

  public void insertIfAbsent(final String initial) {
    dslContext
        .insertInto(Tables.LINKS, Tables.LINKS.ORIGINAL)
        .select(
            DSL.select(DSL.val(initial))
                .whereNotExists(
                    DSL.selectOne().from(Tables.LINKS).where(Tables.LINKS.ORIGINAL.eq(initial))))
        .execute();
  }

  public Integer getId(final String initial) {
    return dslContext
        .select(Tables.LINKS.ID)
        .from(Tables.LINKS)
        .where(Tables.LINKS.ORIGINAL.eq(initial))
        .fetchOneInto(Integer.class);
  }
}
