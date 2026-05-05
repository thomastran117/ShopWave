package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.InternalNote;
import backend.models.enums.NoteEntityType;

import java.util.List;

@Repository
public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {

    List<InternalNote> findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(
            NoteEntityType entityType, long entityId);
}
