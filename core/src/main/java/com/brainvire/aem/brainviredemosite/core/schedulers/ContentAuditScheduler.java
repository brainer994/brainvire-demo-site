package com.brainvire.aem.brainviredemosite.core.schedulers;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.adobe.granite.crypto.CryptoException;
import com.adobe.granite.crypto.CryptoSupport;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Designate(ocd = ContentAuditScheduler.Config.class)
@Component(service = Runnable.class)
public class ContentAuditScheduler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ContentAuditScheduler.class);
    private static final String SERVICE_USER = "contentAuditService";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    @ObjectClassDefinition(name = "Content Audit Scheduler",
            description = "Finds recently modified pages, generates a CSV report and emails it")
    public @interface Config {

        @AttributeDefinition(name = "Cron Expression")
        String scheduler_expression() default "0 0 7 * * ?";

        @AttributeDefinition(name = "Concurrent")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "Search Root Path")
        String searchRootPath() default "/content/brainvire-demo-site";

        @AttributeDefinition(name = "Days Back", description = "Pages modified within last N days")
        int daysBack() default 7;

        @AttributeDefinition(name = "SMTP Host")
        String smtpHost() default "smtp.gmail.com";

        @AttributeDefinition(name = "SMTP Port")
        String smtpPort() default "587";

        @AttributeDefinition(name = "SMTP Username")
        String smtpUsername() default "";

        @AttributeDefinition(name = "SMTP Password")
        String smtpPassword() default "";

        @AttributeDefinition(name = "Mail From")
        String mailFrom() default "";

        @AttributeDefinition(name = "Mail To", description = "Comma-separated recipients")
        String mailTo() default "";

        @AttributeDefinition(name = "Mail Subject")
        String mailSubject() default "AEM Content Audit Report";
    }

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private CryptoSupport cryptoSupport;

    private Config config;

    @Activate
    protected void activate(Config config) {
        this.config = config;
        log.info("ContentAuditScheduler: activated — smtpHost={}, smtpPort={}, smtpUsername={}, mailFrom={}, mailTo={}, searchRootPath={}, daysBack={}",
                config.smtpHost(), config.smtpPort(), config.smtpUsername(),
                config.mailFrom(), config.mailTo(), config.searchRootPath(), config.daysBack());
    }

    private String decryptPassword(String raw) throws MessagingException {
        if (raw == null || raw.isBlank()) {
            throw new MessagingException("SMTP password is not configured");
        }
        log.info("ContentAuditScheduler: smtpPassword — isProtected={}, length={}",
                cryptoSupport.isProtected(raw), raw.length());
        try {
            String decrypted = cryptoSupport.isProtected(raw) ? cryptoSupport.unprotect(raw) : raw;
            log.info("ContentAuditScheduler: smtpPassword decrypted — length={}", decrypted.length());
            return decrypted;
        } catch (CryptoException e) {
            throw new MessagingException("Failed to decrypt SMTP password", e);
        }
    }

    @Override
    public void run() {
        log.info("ContentAuditScheduler: run() triggered — searchRootPath={}, daysBack={}, smtpUsername={}",
                config.searchRootPath(), config.daysBack(), config.smtpUsername());
        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))) {

            byte[] csv = buildCsv(resolver);
            sendMail(csv);
            log.info("ContentAuditScheduler: report sent successfully");

        } catch (LoginException e) {
            log.error("ContentAuditScheduler: failed to obtain service resolver", e);
        } catch (RepositoryException e) {
            log.error("ContentAuditScheduler: JCR query failed", e);
        } catch (java.io.IOException e) {
            log.error("ContentAuditScheduler: failed to build CSV", e);
        } catch (MessagingException e) {
            log.error("ContentAuditScheduler: failed to send email", e);
        }
    }

    private byte[] buildCsv(ResourceResolver resolver) throws RepositoryException, java.io.IOException {
        Session session = resolver.adaptTo(Session.class);
        QueryManager qm = session.getWorkspace().getQueryManager();

        String sinceIso = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(config.daysBack())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00"));

        String sql = "SELECT [jcr:path], [jcr:content/jcr:title], [jcr:content/cq:lastModified] " +
                     "FROM [cq:Page] AS page " +
                     "WHERE ISDESCENDANTNODE(page, '" + config.searchRootPath() + "') " +
                     "AND [jcr:content/cq:lastModified] >= CAST('" + sinceIso + "' AS DATE) " +
                     "ORDER BY [jcr:content/cq:lastModified] DESC";

        log.info("ContentAuditScheduler: executing query — sinceIso={}", sinceIso);
        Query query = qm.createQuery(sql, Query.JCR_SQL2);
        QueryResult result = query.execute();
        RowIterator rows = result.getRows();

        int rowCount = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            writer.write("Title,Path,Last Modified\n");
            while (rows.hasNext()) {
                Row row = rows.nextRow();
                String path = row.getValue("jcr:path").getString();
                String title = safeString(row, "jcr:content/jcr:title", path);
                String modified = safeDate(row, "jcr:content/cq:lastModified");
                writer.write(escapeCsv(title) + "," + escapeCsv(path) + "," + escapeCsv(modified) + "\n");
                rowCount++;
            }
        }
        log.info("ContentAuditScheduler: CSV built — {} pages found, {} bytes", rowCount, baos.size());
        return baos.toByteArray();
    }

    private String safeString(Row row, String col, String fallback) {
        try {
            javax.jcr.Value v = row.getValue(col);
            return v != null ? v.getString() : fallback;
        } catch (RepositoryException e) {
            log.debug("ContentAuditScheduler: could not read column '{}', using fallback", col, e);
            return fallback;
        }
    }

    private String safeDate(Row row, String col) {
        try {
            javax.jcr.Value v = row.getValue(col);
            if (v != null) {
                return DATE_FMT.format(v.getDate().toInstant());
            }
        } catch (RepositoryException e) {
            log.debug("ContentAuditScheduler: could not read date column '{}'", col, e);
        }
        return "";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void sendMail(byte[] csvBytes) throws MessagingException {
        log.info("ContentAuditScheduler: sending email — host={}, port={}, from={}, to={}",
                config.smtpHost(), config.smtpPort(), config.mailFrom(), config.mailTo());
        Properties props = new Properties();
        props.put("mail.smtp.host", config.smtpHost());
        props.put("mail.smtp.port", config.smtpPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        final String password = decryptPassword(config.smtpPassword());
        javax.mail.Session mailSession = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.smtpUsername(), password);
            }
        });

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(config.mailFrom()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.mailTo()));
        message.setSubject(config.mailSubject());

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Please find the AEM content audit report attached.");

        MimeBodyPart attachPart = new MimeBodyPart();
        attachPart.setContent(csvBytes, "text/csv");
        attachPart.setFileName("content-audit-report.csv");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}
