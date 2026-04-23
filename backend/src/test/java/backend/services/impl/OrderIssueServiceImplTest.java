package backend.services.impl;

import backend.dtos.requests.issue.OpenIssueRequest;
import backend.dtos.requests.issue.RejectIssueRequest;
import backend.dtos.requests.issue.ResolveWithCreditRequest;
import backend.dtos.requests.issue.ResolveWithRefundRequest;
import backend.dtos.requests.issue.TransitionIssueRequest;
import backend.dtos.responses.credit.CreditEntryResponse;
import backend.dtos.responses.issue.OrderIssueResponse;
import backend.dtos.responses.return_.ReturnResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ForbiddenException;
import backend.models.core.Order;
import backend.models.core.OrderIssue;
import backend.models.core.User;
import backend.models.enums.CreditEntryType;
import backend.models.enums.OrderIssueState;
import backend.models.enums.OrderIssueType;
import backend.models.enums.UserRole;
import backend.repositories.OrderIssueRepository;
import backend.repositories.OrderRepository;
import backend.repositories.SupportTicketRepository;
import backend.repositories.UserRepository;
import backend.services.intf.CustomerCreditService;
import backend.services.intf.ReplacementOrderService;
import backend.services.intf.ReturnService;
import backend.services.intf.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderIssueServiceImplTest {

    private OrderIssueRepository issueRepository;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private SupportTicketRepository ticketRepository;
    private ReturnService returnService;
    private ReplacementOrderService replacementOrderService;
    private CustomerCreditService customerCreditService;
    private SupportTicketService supportTicketService;
    private OrderIssueServiceImpl service;

    @BeforeEach
    void setUp() {
        issueRepository = mock(OrderIssueRepository.class);
        orderRepository = mock(OrderRepository.class);
        userRepository = mock(UserRepository.class);
        ticketRepository = mock(SupportTicketRepository.class);
        returnService = mock(ReturnService.class);
        replacementOrderService = mock(ReplacementOrderService.class);
        customerCreditService = mock(CustomerCreditService.class);
        supportTicketService = mock(SupportTicketService.class);
        service = new OrderIssueServiceImpl(issueRepository, orderRepository, userRepository,
                ticketRepository, returnService, replacementOrderService,
                customerCreditService, supportTicketService);
    }

    // ─── openIssue ───────────────────────────────────────────────────────────

    @Test
    void openIssue_customerCanOpenForOwnOrder() {
        User customer = makeUser(1L, UserRole.USER);
        Order order = makeOrder(10L, customer);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OpenIssueRequest req = new OpenIssueRequest(OrderIssueType.DAMAGED, "Box was crushed", false);
        OrderIssueResponse resp = service.openIssue(10L, 1L, req);

        assertEquals(OrderIssueState.REPORTED.name(), resp.getState());
        assertEquals(OrderIssueType.DAMAGED.name(), resp.getType());
    }

    @Test
    void openIssue_customerCannotOpenForOtherUsersOrder() {
        User customer = makeUser(1L, UserRole.USER);
        User other = makeUser(2L, UserRole.USER);
        Order order = makeOrder(10L, other);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class,
                () -> service.openIssue(10L, 1L, new OpenIssueRequest(OrderIssueType.DAMAGED, null, false)));
    }

    // ─── transitionState ─────────────────────────────────────────────────────

    @Test
    void transitionState_staffCanAdvanceState() {
        User staff = makeUser(2L, UserRole.SUPPORT);
        OrderIssue issue = makeIssue(5L, makeOrder(10L, makeUser(1L, UserRole.USER)), staff);
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.transitionState(5L, 2L, new TransitionIssueRequest(OrderIssueState.INVESTIGATING));
        assertEquals(OrderIssueState.INVESTIGATING, issue.getState());
    }

    @Test
    void transitionState_customerForbidden() {
        User customer = makeUser(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(ForbiddenException.class,
                () -> service.transitionState(5L, 1L, new TransitionIssueRequest(OrderIssueState.INVESTIGATING)));
    }

    // ─── resolveWithRefund ───────────────────────────────────────────────────

    @Test
    void resolveWithRefund_setsReturnIdAndTerminalState() {
        User staff = makeUser(2L, UserRole.SUPPORT);
        User customer = makeUser(1L, UserRole.USER);
        OrderIssue issue = makeIssue(5L, makeOrder(10L, customer), staff);

        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));
        when(returnService.issuePartialRefund(anyLong(), anyLong(), any(), anyLong()))
                .thenReturn(makeReturnResponse(20L));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.resolveWithRefund(5L, 2L, new ResolveWithRefundRequest(500L, "Compensation"));

        assertEquals(OrderIssueState.RESOLVED_REFUND, issue.getState());
        assertEquals(20L, issue.getReturnId());
        assertNotNull(issue.getResolvedAt());
    }

    // ─── resolveWithCredit ───────────────────────────────────────────────────

    @Test
    void resolveWithCredit_setsCreditIdAndTerminalState() {
        User staff = makeUser(2L, UserRole.SUPPORT);
        User customer = makeUser(1L, UserRole.USER);
        OrderIssue issue = makeIssue(5L, makeOrder(10L, customer), staff);

        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));
        when(customerCreditService.issueCredit(anyLong(), any(), anyLong(), any(), any()))
                .thenReturn(makeCreditResponse(30L));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.resolveWithCredit(5L, 2L, new ResolveWithCreditRequest(300L, "Sorry"));

        assertEquals(OrderIssueState.RESOLVED_CREDIT, issue.getState());
        assertEquals(30L, issue.getCustomerCreditId());
    }

    // ─── rejectIssue ─────────────────────────────────────────────────────────

    @Test
    void rejectIssue_setsRejectedStateAndReason() {
        User staff = makeUser(2L, UserRole.SUPPORT);
        User customer = makeUser(1L, UserRole.USER);
        OrderIssue issue = makeIssue(5L, makeOrder(10L, customer), staff);

        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectIssue(5L, 2L, new RejectIssueRequest("No evidence provided"));

        assertEquals(OrderIssueState.REJECTED, issue.getState());
        assertEquals("No evidence provided", issue.getRejectionReason());
    }

    @Test
    void resolveWithRefund_throwsWhenIssueAlreadyTerminal() {
        User staff = makeUser(2L, UserRole.SUPPORT);
        User customer = makeUser(1L, UserRole.USER);
        OrderIssue issue = makeIssue(5L, makeOrder(10L, customer), staff);
        issue.setState(OrderIssueState.RESOLVED_REFUND);

        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));

        assertThrows(BadRequestException.class,
                () -> service.resolveWithRefund(5L, 2L, new ResolveWithRefundRequest(100L, null)));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail("user" + id + "@test.com");
        u.setRole(role);
        return u;
    }

    private Order makeOrder(long id, User owner) {
        Order o = new Order();
        o.setId(id);
        o.setUser(owner);
        return o;
    }

    private OrderIssue makeIssue(long id, Order order, User reporter) {
        OrderIssue i = new OrderIssue();
        i.setId(id);
        i.setOrder(order);
        i.setReportedBy(reporter);
        i.setType(OrderIssueType.DAMAGED);
        i.setState(OrderIssueState.REPORTED);
        return i;
    }

    private ReturnResponse makeReturnResponse(long id) {
        return new ReturnResponse(id, 10L, null, "COMPLETED", null, null, null,
                false, List.of(), List.of(), null, null, null, null,
                500L, "PENDING", Instant.now(), Instant.now(), Instant.now(), Instant.now());
    }

    private CreditEntryResponse makeCreditResponse(long id) {
        return new CreditEntryResponse(id, 300L, "USD", CreditEntryType.COMPENSATION_ISSUED.name(),
                "Sorry", null, null, null, null, null, Instant.now());
    }
}
