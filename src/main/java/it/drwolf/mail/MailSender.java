package it.drwolf.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import it.drwolf.base.interfaces.Loggable;
import play.Environment;

/**
 * application.conf example:
 * <p>
 * email.enabled=true
 * email.secure=false
 * email.from="no-reply@drwolf.it"
 * email.name="YOUR_APP_NAME"
 * smtp.user="user"
 * smtp.password="password"
 * smtp.server=localhost
 * smtp.port=25
 * smtp.trap="firstAddresseeOfAllMails@drwolf.it, secondAddresseeOfAllMails@drwolf.it"
 */
@Singleton
public class MailSender implements Loggable {

	private Logger logger = this.logger();

	private Config configuration;

	private boolean enabled = false;

	private Environment environment;

	private String from;

	private String name;

	private String hostname;

	private boolean secure = false;

	private String smtpPassword;

	private Integer smtpPort;

	private String smtpTrap;

	private String smtpUser;

	@Inject
	public MailSender(Config configuration, Environment environment) {
		this.configuration = configuration;
		this.environment = environment;
		this.getEmailParamater();
	}

	private void doSendHtml(String subject, String htmlMsg, List<String> tos, JsonNode body) throws EmailException {

		Iterator<String> fieldNames = body.fieldNames();

		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode fieldValue = body.get(fieldName);
			if (fieldName.equals("bodyVariables")) {
				ArrayNode anBodyVariables = (ArrayNode) fieldValue;
				for (JsonNode jn : anBodyVariables) {
					Iterator<String> fieldNameBody = jn.fieldNames();
					while (fieldNameBody.hasNext()) {
						String fn = fieldNameBody.next();
						JsonNode fv = jn.get(fn);
						htmlMsg = htmlMsg.replaceAll("\\{\\{" + fn + "\\}\\}", fv == null ? "" : fv.asText());
					}
				}
			} else {
				htmlMsg = htmlMsg.replaceAll("\\{\\{" + fieldName + "\\}\\}",
						fieldValue == null ? "" : fieldValue.asText());
			}
		}

		HtmlEmail email = new HtmlEmail();
		email.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
		if (this.secure) {
			email.setSSLOnConnect(true);
			email.setStartTLSEnabled(true);
		}
		email.setHostName(this.hostname);
		email.setSmtpPort(this.smtpPort);
		email.setAuthenticator(new DefaultAuthenticator(this.smtpUser, this.smtpPassword));

		email.setFrom(this.from, this.name);
		email.setSubject(subject);
		if (this.environment.isDev()) {
			email.addTo(this.smtpTrap);
		} else {
			email.addTo(tos.toArray(new String[tos.size()]));
		}
		email.setHtmlMsg(htmlMsg);

		this.logger.debug("Mail To     : " + tos);
		this.logger.debug("Mail Subject: " + subject);
		email.send();

		this.logger.debug(String.format("Mail '%s' to %s sent", subject, tos));
	}

	private void doTxtHtml(String subject, String body, List<String> tos, List<String> toBcc, byte[] objToAttach,
			String nameOfAttach) throws EmailException, AddressException, MessagingException, IOException {

		MultiPartEmail email = new MultiPartEmail();
		if (this.secure) {
			email.setSSLOnConnect(true);
			email.setStartTLSEnabled(true);
		}
		email.setHostName(this.hostname);
		email.setSmtpPort(this.smtpPort);
		email.setAuthenticator(new DefaultAuthenticator(this.smtpUser, this.smtpPassword));

		if (tos != null) {
			for (String emailTo : tos) {
				email.addTo(emailTo);
			}
		}

		if (toBcc != null) {
			for (String emailToBcc : toBcc) {
				email.addBcc(emailToBcc);
			}
		}

		email.setFrom(this.from, this.name);
		email.setSubject(subject);
		email.setMsg(body);

		if (objToAttach != null && nameOfAttach != null) {
			// get your inputstream from your db
			InputStream is = new ByteArrayInputStream(objToAttach);

			DataSource source = new ByteArrayDataSource(is, "application/pdf");

			// add the attachment
			email.attach(source, nameOfAttach, "Description of some file");
		}
		// send the email
		email.send();

		if (tos != null) {
			this.logger.debug(String.format("Mail \"%s\" sent to %s from %s", subject, tos, this.from));
		}
		if (toBcc != null) {
			this.logger.debug(String.format("Mail \"%s\" sent to BCC %s from %s", subject, toBcc, this.from));
		}
	}

	private List<String> extractMailTraps() {
		return Arrays.asList(this.smtpTrap.split(",")).stream().map(to -> to.trim()).collect(Collectors.toList());
	}

	private void getEmailParamater() {
		this.hostname = this.configuration.getString("smtp.server");
		this.smtpPort = this.configuration.getInt("smtp.port");
		this.smtpUser = this.configuration.getString("smtp.user");
		this.smtpPassword = this.configuration.getString("smtp.password");
		this.from = this.configuration.getString("email.from");
		this.name = this.configuration.getString("email.name");
		this.secure = this.configuration.getBoolean("email.secure");
		this.enabled = this.configuration.getBoolean("email.enabled");
		try {
			this.smtpTrap = this.configuration.getString("smtp.trap");
		} catch (ConfigException.Missing e) {
			this.logger.debug("No mail trap set");
		} catch (Exception e) {
			this.logger.error("Error reading mail trap value", e);
		}
	}

	public void sendHtmlEmail(String templateClass, String subject, List<String> tos, JsonNode body)
			throws EmailException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SecurityException {

		if (this.name != null && !this.name.trim().isEmpty()) {
			subject = String.format("[%s] ", this.name) + subject;
		}

		if (tos == null || tos.isEmpty()) {
			this.logger.warn(String.format("Mail '%s' without addresseses. It wont be sent!", subject));
			return;
		}

		Class<?> c = Class.forName(templateClass);
		String template = c.getDeclaredMethod("render").invoke(null).toString();

		if (this.enabled) {
			if (this.smtpTrap != null && !this.smtpTrap.trim().isEmpty()) {
				String removed = tos.stream().collect(Collectors.joining(", "));
				tos = this.extractMailTraps();
				this.logger.info(
						String.format("[%s=%s] Addressees '%s' replaced", "smtp.trap", this.smtpTrap, removed));
			}
			this.doSendHtml(subject, template, tos, body);
		} else {
			this.logger.info(String.format("[%s=false] Email '%s' NOT SENT to %s", "email.enabled", subject, tos));
		}

	}

	public void sendTxtEmail(String subject, String body, List<String> tos, List<String> toBcc, byte[] objToAttach,
			String nameOfAttach) throws EmailException, AddressException, MessagingException, IOException {

		if (this.name != null && !this.name.trim().isEmpty()) {
			subject = String.format("[%s] ", this.name) + subject;
		}

		if ((tos == null || tos.isEmpty()) && (toBcc == null || toBcc.isEmpty())) {
			this.logger.warn(String.format("Mail '%s' without addresseses. It wont be sent!", subject));
			return;
		}

		if (this.enabled) {
			if (this.smtpTrap != null && !this.smtpTrap.trim().isEmpty()) {
				if (tos != null && !tos.isEmpty()) {
					String removed = tos.stream().collect(Collectors.joining(", "));
					tos = this.extractMailTraps();
					this.logger.info(
							String.format("[%s=%s] Addressees '%s' replaced", "smtp.trap", this.smtpTrap, removed));
				}
				if (toBcc != null && !toBcc.isEmpty()) {
					String bccRemoved = toBcc.stream().collect(Collectors.joining(", "));
					toBcc = this.extractMailTraps();
					this.logger.info(String.format("[%s=%s] BCC Addressees '%s' replaced", "smtp.trap", this.smtpTrap,
							bccRemoved));
				}
			}
			this.doTxtHtml(subject, body, tos, toBcc, objToAttach, nameOfAttach);
		} else {
			this.logger.info(String.format("[%s=false] Email '%s' NOT SENT to %s", "email.enabled", subject, tos));
		}
	}
}
