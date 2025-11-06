package anton.asmirko.app.repository.linksseq;

import com.example.jooq.generated.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinksSequenceRepository {

  private final DSLContext dslContext;

  public int getNext() {
    Integer curVal =
        dslContext
            .select(Tables.LINKS_SEQUENCE.VAL)
            .from(Tables.LINKS_SEQUENCE)
            .where(Tables.LINKS_SEQUENCE.NAME.eq("links_seq"))
            .fetchOne(Tables.LINKS_SEQUENCE.VAL);
    if (curVal == null) {
      throw new IllegalStateException("Проблемы с конфигурацией БД");
    }
    int newVal = curVal + 1;
    dslContext
        .update(Tables.LINKS_SEQUENCE)
        .set(Tables.LINKS_SEQUENCE.VAL, curVal + 1)
        .where(Tables.LINKS_SEQUENCE.NAME.eq("links_seq"))
        .execute();
    return newVal;
  }
}
