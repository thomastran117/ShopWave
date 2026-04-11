package backend.services.impl;

import backend.configurations.environment.EnvironmentSetting;
import backend.dtos.responses.order.OrderItemResponse;
import backend.dtos.responses.order.OrderResponse;
import backend.services.intf.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EnvironmentSetting env;
    private final RetryTemplate retryTemplate;

    public EmailServiceImpl(JavaMailSender mailSender,
                            EnvironmentSetting env,
                            @Qualifier("emailRetryTemplate") RetryTemplate retryTemplate) {
        this.mailSender = mailSender;
        this.env = env;
        this.retryTemplate = retryTemplate;
    }

    // ─── Shared HTML shell ────────────────────────────────────────────────────

    /** Wraps content in the shared branded shell (header bar + card + footer). */
    private String wrapInShell(String headerLabel, String bodyHtml) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background-color:#EFF6FF;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" role="presentation">
                <tr>
                  <td align="center" style="padding:48px 16px;">

                    <!-- Card -->
                    <table width="580" cellpadding="0" cellspacing="0" role="presentation"
                           style="background:#ffffff;border-radius:12px;overflow:hidden;
                                  box-shadow:0 4px 24px rgba(30,64,175,0.10);">

                      <!-- Top accent bar -->
                      <tr>
                        <td style="background:linear-gradient(90deg,#1D4ED8 0%%,#3B82F6 100%%);
                                   height:5px;font-size:0;line-height:0;">&nbsp;</td>
                      </tr>

                      <!-- Header -->
                      <tr>
                        <td style="padding:32px 40px 0 40px;">
                          <table width="100%%" cellpadding="0" cellspacing="0" role="presentation">
                            <tr>
                              <td>
                                <span style="font-size:20px;font-weight:800;letter-spacing:-0.5px;color:#1D4ED8;">
                                  ShopWave
                                </span>
                              </td>
                              <td align="right">
                                <span style="font-size:11px;font-weight:600;letter-spacing:0.08em;
                                             text-transform:uppercase;color:#93C5FD;">
                                  %s
                                </span>
                              </td>
                            </tr>
                          </table>
                          <hr style="border:none;border-top:1px solid #DBEAFE;margin:20px 0 0 0;">
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:28px 40px 36px 40px;">
                          %s
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#F8FAFF;border-top:1px solid #DBEAFE;
                                   padding:18px 40px;border-radius:0 0 12px 12px;">
                          <p style="margin:0;font-size:11px;color:#94A3B8;line-height:1.6;">
                            ShopWave &mdash; 123 Commerce Street, San Francisco, CA 94105<br>
                            You are receiving this email because of activity on your account.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(headerLabel, bodyHtml);
    }

    // ─── Button helper ────────────────────────────────────────────────────────

    private String primaryButton(String href, String label) {
        return """
            <a href="%s"
               style="display:inline-block;margin-top:28px;
                      background:linear-gradient(135deg,#1D4ED8 0%%,#3B82F6 100%%);
                      color:#ffffff;padding:14px 36px;border-radius:8px;
                      text-decoration:none;font-size:15px;font-weight:700;
                      letter-spacing:0.02em;box-shadow:0 4px 14px rgba(59,130,246,0.40);">
              %s
            </a>
            """.formatted(href, label);
    }

    // ─── Expiry note helper ───────────────────────────────────────────────────

    private String expiryNote(String text) {
        return """
            <p style="margin:20px 0 0 0;font-size:12px;color:#94A3B8;line-height:1.6;">
              %s
            </p>
            """.formatted(text);
    }

    // ─── Send methods ─────────────────────────────────────────────────────────

    @Async("emailExecutor")
    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = env.getEmail().getVerificationBaseUrl() + "/verify-email?token=" + token;
        String htmlBody = buildVerificationHtml(toEmail, verifyUrl);
        sendMimeMessage(toEmail, "Verify your ShopWave account", htmlBody);
    }

    @Async("emailExecutor")
    @Override
    public void sendDeviceVerificationEmail(String toEmail, String token,
                                            String browser, String os, String ip) {
        String verifyUrl = env.getEmail().getVerificationBaseUrl() + "/verify-device?token=" + token;
        String htmlBody = buildDeviceVerificationHtml(toEmail, verifyUrl, browser, os, ip);
        sendMimeMessage(toEmail, "Verify new device \u2014 ShopWave", htmlBody);
    }

    @Async("emailExecutor")
    @Override
    public void sendOrderReceiptEmail(String toEmail, String firstName, OrderResponse order) {
        String htmlBody = buildOrderReceiptHtml(toEmail, firstName, order);
        sendMimeMessage(toEmail, "Your ShopWave order #" + order.getId() + " \u2014 Receipt", htmlBody);
    }

    private void sendMimeMessage(String toEmail, String subject, String htmlBody) {
        retryTemplate.execute(context -> {
            MimeMessage message = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
                helper.setFrom(env.getEmail().getFrom());
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            mailSender.send(message);
            return null;
        });
    }

    // ─── Email templates ──────────────────────────────────────────────────────

    private String buildVerificationHtml(String email, String verifyUrl) {
        String body = """
            <h1 style="margin:0 0 8px 0;font-size:26px;font-weight:800;
                        color:#0F172A;letter-spacing:-0.5px;">
              Verify your email address
            </h1>
            <p style="margin:0 0 4px 0;font-size:15px;color:#475569;line-height:1.7;">
              Welcome to ShopWave! You signed up with <strong style="color:#1D4ED8;">%s</strong>.
            </p>
            <p style="margin:0;font-size:15px;color:#475569;line-height:1.7;">
              Click the button below to activate your account. This link is valid for 24&nbsp;hours.
            </p>
            %s
            %s
            """.formatted(
                email,
                primaryButton(verifyUrl, "Verify Email Address"),
                expiryNote("If you did not create an account, you can safely ignore this email.")
            );
        return wrapInShell("Account Activation", body);
    }

    private String buildDeviceVerificationHtml(String email, String verifyUrl,
                                               String browser, String os, String ip) {
        String body = """
            <!-- Alert strip -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation"
                   style="background:#EFF6FF;border:1px solid #BFDBFE;border-radius:8px;margin-bottom:24px;">
              <tr>
                <td style="padding:14px 18px;">
                  <p style="margin:0;font-size:13px;font-weight:700;color:#1D4ED8;letter-spacing:0.02em;">
                    &#x26A0;&#xFE0F;&nbsp; New device detected
                  </p>
                  <p style="margin:4px 0 0 0;font-size:13px;color:#3B82F6;">
                    A login was attempted on <strong>%s</strong> from an unrecognised device.
                  </p>
                </td>
              </tr>
            </table>

            <h1 style="margin:0 0 8px 0;font-size:24px;font-weight:800;
                        color:#0F172A;letter-spacing:-0.5px;">
              New Device Login Attempt
            </h1>
            <p style="margin:0 0 20px 0;font-size:15px;color:#475569;line-height:1.7;">
              If this was you, verify the device below to continue. If not, change your password immediately.
            </p>

            <!-- Device detail table -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation"
                   style="border-collapse:collapse;border-radius:8px;overflow:hidden;
                          border:1px solid #DBEAFE;">
              <tr style="background:#F0F7FF;">
                <td style="padding:10px 16px;font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#64748B;width:140px;">Browser</td>
                <td style="padding:10px 16px;font-size:14px;color:#0F172A;font-weight:500;">%s</td>
              </tr>
              <tr style="background:#ffffff;border-top:1px solid #DBEAFE;">
                <td style="padding:10px 16px;font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#64748B;">OS</td>
                <td style="padding:10px 16px;font-size:14px;color:#0F172A;font-weight:500;">%s</td>
              </tr>
              <tr style="background:#F0F7FF;border-top:1px solid #DBEAFE;">
                <td style="padding:10px 16px;font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#64748B;">IP Address</td>
                <td style="padding:10px 16px;font-size:14px;color:#0F172A;font-weight:500;">%s</td>
              </tr>
            </table>

            %s
            %s
            """.formatted(
                email,
                browser, os, ip,
                primaryButton(verifyUrl, "Verify Device"),
                expiryNote("This link expires in <strong>10 minutes</strong>. "
                         + "If you did not attempt to log in, please change your password immediately.")
            );
        return wrapInShell("Security Alert", body);
    }

    private String buildOrderReceiptHtml(String email, String firstName, OrderResponse order) {
        String greeting = (firstName != null && !firstName.isBlank()) ? firstName : email;
        String dateStr = order.getCreatedAt() != null
                ? DateTimeFormatter.ofPattern("MMMM d, yyyy")
                        .withZone(ZoneId.of("UTC"))
                        .format(order.getCreatedAt())
                : "—";
        String currency = order.getCurrency() != null ? order.getCurrency().toUpperCase() : "USD";

        // Build item rows
        StringBuilder itemRows = new StringBuilder();
        for (OrderItemResponse item : order.getItems()) {
            String name = item.getBundleName() != null ? item.getBundleName() : item.getProductName();
            String variant = item.getVariantTitle() != null
                    ? "<br><span style=\"font-size:12px;color:#94A3B8;\">" + item.getVariantTitle() + "</span>"
                    : "";
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemRows.append("""
                <tr>
                  <td style="padding:12px 0;border-bottom:1px solid #EFF6FF;
                             font-size:14px;color:#0F172A;font-weight:500;line-height:1.4;">
                    %s%s
                  </td>
                  <td style="padding:12px 0;border-bottom:1px solid #EFF6FF;
                             font-size:14px;color:#64748B;text-align:center;">%d</td>
                  <td style="padding:12px 0;border-bottom:1px solid #EFF6FF;
                             font-size:14px;color:#0F172A;font-weight:600;text-align:right;">
                    %s&nbsp;%.2f
                  </td>
                </tr>
                """.formatted(name, variant, item.getQuantity(), currency, lineTotal));
        }

        String body = """
            <!-- Order confirmed banner -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation"
                   style="background:linear-gradient(135deg,#EFF6FF 0%%,#DBEAFE 100%%);
                          border:1px solid #BFDBFE;border-radius:10px;margin-bottom:24px;">
              <tr>
                <td style="padding:18px 20px;">
                  <p style="margin:0;font-size:20px;">&#x2705;</p>
                  <p style="margin:4px 0 0 0;font-size:15px;font-weight:700;color:#1D4ED8;">
                    Order Confirmed
                  </p>
                  <p style="margin:2px 0 0 0;font-size:13px;color:#3B82F6;">
                    Hi <strong>%s</strong>, your order has been received and is being processed.
                  </p>
                </td>
              </tr>
            </table>

            <!-- Order meta -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation"
                   style="margin-bottom:24px;">
              <tr>
                <td style="font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#94A3B8;padding-bottom:4px;">Order</td>
                <td style="font-size:14px;color:#0F172A;font-weight:600;
                           text-align:right;padding-bottom:4px;">#%d</td>
              </tr>
              <tr>
                <td style="font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#94A3B8;padding-bottom:4px;">Date</td>
                <td style="font-size:14px;color:#0F172A;text-align:right;padding-bottom:4px;">%s</td>
              </tr>
              <tr>
                <td style="font-size:12px;font-weight:700;letter-spacing:0.06em;
                           text-transform:uppercase;color:#94A3B8;">Status</td>
                <td style="text-align:right;">
                  <span style="display:inline-block;padding:3px 10px;border-radius:20px;
                               background:#DBEAFE;color:#1D4ED8;font-size:12px;font-weight:700;">
                    %s
                  </span>
                </td>
              </tr>
            </table>

            <hr style="border:none;border-top:2px solid #EFF6FF;margin:0 0 20px 0;">

            <!-- Items table -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation">
              <thead>
                <tr>
                  <th style="font-size:11px;font-weight:700;letter-spacing:0.08em;
                             text-transform:uppercase;color:#94A3B8;text-align:left;
                             padding-bottom:10px;">Item</th>
                  <th style="font-size:11px;font-weight:700;letter-spacing:0.08em;
                             text-transform:uppercase;color:#94A3B8;text-align:center;
                             padding-bottom:10px;">Qty</th>
                  <th style="font-size:11px;font-weight:700;letter-spacing:0.08em;
                             text-transform:uppercase;color:#94A3B8;text-align:right;
                             padding-bottom:10px;">Total</th>
                </tr>
              </thead>
              <tbody>
                %s
              </tbody>
            </table>

            <!-- Order total -->
            <table width="100%%" cellpadding="0" cellspacing="0" role="presentation"
                   style="margin-top:16px;background:#EFF6FF;border-radius:8px;">
              <tr>
                <td style="padding:14px 16px;font-size:15px;font-weight:800;color:#0F172A;">
                  Order Total
                </td>
                <td style="padding:14px 16px;font-size:18px;font-weight:800;
                           color:#1D4ED8;text-align:right;">
                  %s&nbsp;%.2f
                </td>
              </tr>
            </table>
            """.formatted(
                greeting,
                order.getId(), dateStr, order.getStatus(),
                itemRows.toString(),
                currency, order.getTotalAmount()
            );

        return wrapInShell("Order Receipt", body);
    }
}