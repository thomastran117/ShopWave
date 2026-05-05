package backend.services.intf.orders;

import backend.dtos.requests.issue.OpenIssueRequest;
import backend.dtos.requests.issue.RejectIssueRequest;
import backend.dtos.requests.issue.ResolveWithCreditRequest;
import backend.dtos.requests.issue.ResolveWithRefundRequest;
import backend.dtos.requests.issue.ResolveWithReplacementRequest;
import backend.dtos.requests.issue.TransitionIssueRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.issue.OrderIssueResponse;
import backend.models.enums.OrderIssueState;

import java.util.List;

public interface OrderIssueService {

    /** Opens a new issue for an order. Both customers and staff can report. */
    OrderIssueResponse openIssue(long orderId, long reporterUserId, OpenIssueRequest request);

    /** Lists all issues for a specific order (customer scoped to own orders; staff unrestricted). */
    List<OrderIssueResponse> getIssuesByOrder(long orderId, long actorUserId);

    /** Staff triage list — filter by state. */
    PagedResponse<OrderIssueResponse> listIssues(long actorUserId, OrderIssueState state, int page, int size);

    /** Staff-only: advance the issue through the state machine. */
    OrderIssueResponse transitionState(long issueId, long actorUserId, TransitionIssueRequest request);

    /** Staff-only: resolve via Stripe refund (delegates to ReturnService). */
    OrderIssueResponse resolveWithRefund(long issueId, long actorUserId, ResolveWithRefundRequest request);

    /** Staff-only: resolve by creating a replacement order. */
    OrderIssueResponse resolveWithReplacement(long issueId, long actorUserId, ResolveWithReplacementRequest request);

    /** Staff-only: resolve by issuing store credit. */
    OrderIssueResponse resolveWithCredit(long issueId, long actorUserId, ResolveWithCreditRequest request);

    /** Staff-only: reject the issue with a reason. */
    OrderIssueResponse rejectIssue(long issueId, long actorUserId, RejectIssueRequest request);
}
