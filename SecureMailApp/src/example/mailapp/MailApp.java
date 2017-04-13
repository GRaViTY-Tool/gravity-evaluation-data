package example.mailapp;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 * WARNING: This class has been implemented to represent bad design ant to contain various security issues
 * 
 * This class is a Blob
 */

public class MailApp {

	protected Hashtable<String, Contact> contacts = new Hashtable<String, Contact>();
	protected List<Message> outbox = new LinkedList<Message>();

	private String user, password, server, sender;
	private Properties properties;

	public static void main(String[] args) {
		new MailApp(args).run();
	}

	public MailApp(String[] args) {
		parseArgs(args);
	}

	protected void run() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				send();
			}
		}, 0, 60000);
		while (true) {
			Scanner scanner = new Scanner(System.in);
			String next = scanner.nextLine().trim();
			if ("exit".equals(next)) {
				timer.cancel();
				scanner.close();
				System.exit(0);
			} else if (next.startsWith("send")) {
				List<String> receivers = new LinkedList<String>();
				String text = null;
				String[] commands = next.split(" ");
				for (int i = 1; i < commands.length; i++) {
					String command = commands[i];
					if ("-r".equals(command)) {
						String r;
						while (i + 1 < commands.length && !(r = commands[i + 1]).startsWith("-")) {
							while (r.startsWith("\"") && !r.endsWith("\"")) {
								r += " " + commands[++i + 1];
							}
							if (r.startsWith("\"")) {
								r = r.substring(1, r.length() - 1);
							}
							receivers.add(r);
							i++;
						}
					} else if ("-m".equals(command)) {
						text = commands[i + 1];
						while (text.startsWith("\"") && !text.endsWith("\"")) {
							text += " " + commands[++i + 1];
						}
						if (text.startsWith("\"")) {
							text = text.substring(1, text.length() - 1);
						}
						i++;
					} else {
						System.out.println("Unknown send parameter: " + command);
						break;
					}
				}
				if (receivers.size() == 0) {
					System.out.println("No receiver is given.");
				} else if (text == null) {
					System.out.println("Message has no text.");
				} else {
					scedule(text, receivers);
				}

			} else if (next.startsWith("add")) {
				List<String> commands = Arrays.asList(next.split(" "));
				if (commands.size() == 4) {
					Contact contact = new Contact();
					contact.setName(commands.get(1));
					contact.setSurname(commands.get(2));
					contact.setMail(commands.get(3));
					addContact(contact);
				} else {
					System.out.println("The expected format is: \"add <Name> <Surname> <mail@example.domain\"");
				}
			} else if (next.startsWith("remove")) {
				String id = next.substring("remove ".length()).trim();
				if (!removeContact(id)) {
					System.out.println("The contact \"" + id + "\" couldn't be removed from the contact list.");
				}
			} else {
				System.out.println("Unknown command");
			}

		}
	}

	protected String[] parseArgs(String[] args) {
		List<String> notRecognized = new LinkedList<String>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if ("-a".equals(arg.trim())) {
				String location = args[++i];
				if (location == null || "".equals(location)) {
					throw new RuntimeException("Path to contacts is not valid");
				}
			} else if ("-o".equals(arg)) {
				server = args[++i];
			} else if ("-ou".equals(arg)) {
				user = args[++i];
			} else if ("-op".equals(arg)) {
				password = args[++i];
			} else if ("-s".equals(arg)) {
				sender = args[++i];
			} else {
				notRecognized.add(arg);
			}
		}
		if (server == null) {
			throw new RuntimeException("No mail server is given");
		}
		if (user == null) {
			throw new RuntimeException();
		}
		if (password == null) {
			throw new RuntimeException();
		}
		properties = System.getProperties();
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", server);
		properties.put("mail.smtp.user", user);
		properties.put("mail.smtp.password", password);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");

		return notRecognized.toArray(new String[notRecognized.size()]);
	}

	private void send() {
		while (outbox.size() > 0) {
			Message mail = outbox.remove(0);
			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			});
			javax.mail.Message message = new MimeMessage(session);
			try {
				message.setFrom(InternetAddress.parse(sender)[0]);
				for (String receiver : mail.getReceivers()) {
					message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(receiver));
				}
				message.setText(mail.getText());
				Transport.send(message);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void scedule(String message, List<String> receivers) {
		Pattern regex = Pattern.compile(".+@.+\\.[a-z]+");
		List<String> mail = new LinkedList<String>();
		for (String text : receivers) {
			if (regex.matcher(text).matches()) {
				mail.add(text);
			} else if (contacts.containsKey(text)) {
				mail.add(contacts.get(text).getMail());
			} else {
				System.out.println("The receiver \"" + text + "\" is no mail address or contact name.");
			}
		}
		outbox.add(new Message(message, mail));
	}

	public Contact getContact(String id) {
		return contacts.get(id);
	}

	public void addContact(Contact contact) {
		contacts.put(contact.getName() + " " + contact.getSurname(), contact);
	}

	public boolean removeContact(String id) {
		if (contacts.containsKey(id)) {
			return contacts.remove(id) != null;
		}
		return false;
	}
}
