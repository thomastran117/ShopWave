package backend.services.impl;

import backend.dtos.requests.note.CreateNoteRequest;
import backend.dtos.responses.note.InternalNoteResponse;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.InternalNote;
import backend.models.core.User;
import backend.models.enums.NoteEntityType;
import backend.models.enums.UserRole;
import backend.repositories.InternalNoteRepository;
import backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalNoteServiceImplTest {

    private InternalNoteRepository noteRepository;
    private UserRepository userRepository;
    private InternalNoteServiceImpl service;

    @BeforeEach
    void setUp() {
        noteRepository = mock(InternalNoteRepository.class);
        userRepository = mock(UserRepository.class);
        service = new InternalNoteServiceImpl(noteRepository, userRepository);
    }

    // ─── addNote ─────────────────────────────────────────────────────────────

    @Test
    void addNote_staffCanAddNote() {
        User staff = makeUser(1L, UserRole.SUPPORT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(noteRepository.save(any())).thenAnswer(inv -> {
            InternalNote n = inv.getArgument(0);
            n.setAuthor(staff);
            return n;
        });

        CreateNoteRequest req = new CreateNoteRequest(NoteEntityType.ORDER, 10L, "Checked with warehouse");
        service.addNote(1L, req);

        verify(noteRepository).save(any(InternalNote.class));
    }

    @Test
    void addNote_customerCannotAddNote() {
        User customer = makeUser(2L, UserRole.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        CreateNoteRequest req = new CreateNoteRequest(NoteEntityType.ORDER, 10L, "note");
        assertThrows(ForbiddenException.class, () -> service.addNote(2L, req));
    }

    // ─── listNotes ───────────────────────────────────────────────────────────

    @Test
    void listNotes_staffSeeNotes() {
        User staff = makeUser(1L, UserRole.SUPPORT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(noteRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(NoteEntityType.TICKET, 5L))
                .thenReturn(List.of());

        List<InternalNoteResponse> notes = service.listNotes(1L, NoteEntityType.TICKET, 5L);
        assertNotNull(notes);
        assertTrue(notes.isEmpty());
    }

    @Test
    void listNotes_customerForbidden() {
        User customer = makeUser(2L, UserRole.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        assertThrows(ForbiddenException.class, () -> service.listNotes(2L, NoteEntityType.TICKET, 5L));
    }

    // ─── deleteNote ──────────────────────────────────────────────────────────

    @Test
    void deleteNote_authorCanDelete() {
        User staff = makeUser(1L, UserRole.SUPPORT);
        InternalNote note = makeNote(10L, staff);
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        service.deleteNote(10L, 1L);
        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNote_adminCanDeleteOthersNotes() {
        User author = makeUser(1L, UserRole.SUPPORT);
        User admin = makeUser(2L, UserRole.ADMIN);
        InternalNote note = makeNote(10L, author);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        service.deleteNote(10L, 2L);
        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNote_nonAuthorStaffForbidden() {
        User author = makeUser(1L, UserRole.SUPPORT);
        User other = makeUser(3L, UserRole.SUPPORT);
        InternalNote note = makeNote(10L, author);
        when(userRepository.findById(3L)).thenReturn(Optional.of(other));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        assertThrows(ForbiddenException.class, () -> service.deleteNote(10L, 3L));
    }

    @Test
    void deleteNote_throwsWhenNoteNotFound() {
        User staff = makeUser(1L, UserRole.SUPPORT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteNote(99L, 1L));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail("user" + id + "@test.com");
        u.setRole(role);
        return u;
    }

    private InternalNote makeNote(long id, User author) {
        InternalNote n = new InternalNote();
        n.setId(id);
        n.setAuthor(author);
        n.setEntityType(NoteEntityType.ORDER);
        n.setEntityId(1L);
        n.setBody("test note");
        return n;
    }
}
