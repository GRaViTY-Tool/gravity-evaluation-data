package example.mailapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.gravity.security.annotations.requirements.Critical;
import org.gravity.security.annotations.requirements.High;

/*
 * WARNING: This class has been implemented to represent bad design ant to contain various security issues
 * 
 * This class is a Blob
 */

@Critical(high={"RsaAdapter.sign(String):String"})
public class SecureMailApp extends MailApp {
	
	private String password = null;
	private String alias = "myID";
	
	KeyStore keys;
	
	public static void main(String[] args) {
		new SecureMailApp(args).run();
	}
	
	public SecureMailApp(String[] args) {
		super(args);
		try {
			keys = KeyStore.getInstance("JKS");
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}
	
	protected String[] parseArgs(String[] args){
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		
		List<String> left = new LinkedList<String>();
		for(int i = 0; i< args.length; i++){
			String arg = args[i];
			if("-kp".equals(arg)){
				password = args[++i];
			}
			else if("-k".equals(arg)){
				try {
					byte[] bytes;
				
					String value = args[++i];
					File file = new File(value);
					if(file.exists()){
						bytes = Files.readAllBytes(file.toPath());
					}
					else{
						bytes = value.getBytes();
					}
					KeySpec spec = new PKCS8EncodedKeySpec(bytes);
				    KeyFactory factory = KeyFactory.getInstance("RSA");
					privateKey = factory.generatePrivate(spec);
					RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey; 
					publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent()));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if("-c".equals(arg)){
				try {
					byte[] bytes;
					
					String value = args[++i];
					File file = new File(value);
					if(file.exists()){
						bytes = Files.readAllBytes(file.toPath());
					}
					else{
						bytes = value.getBytes();
					}
					KeySpec spec = new X509EncodedKeySpec(bytes);
				    KeyFactory factory = KeyFactory.getInstance("RSA", "SUN");
					publicKey = factory.generatePublic(spec);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				left.add(arg);
			}
		}
		if(password == null){
			throw new RuntimeException("Password to private key is missing.");
		}
		if(privateKey == null || publicKey == null){
			System.out.println("No valid keypair given. Generating new keypair");
			try {
				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				generator.initialize(1024, random);
				KeyPair pair = generator.generateKeyPair();
				publicKey = pair.getPublic();
				privateKey = pair.getPrivate();
				System.out.println("Keys generated.");
				Scanner scanner = new Scanner(System.in);
				System.out.println("Enter location for private key.");
				FileOutputStream privOut = null;
				try {
					privOut = new FileOutputStream(new File(scanner.nextLine()));
					privOut.write(privateKey.getEncoded());
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						if(privOut!=null)privOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Enter location for public key.");
				FileOutputStream pubOut = null;
				try {
					pubOut = new FileOutputStream(new File(scanner.nextLine()));
					pubOut.write(privateKey.getEncoded());
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						if(pubOut!=null)pubOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

		}
		try {
			keys.setKeyEntry(alias, privateKey.getEncoded(), null);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return super.parseArgs(left.toArray(new String[left.size()]));
	}

	@Override
	public void schedule(String message, List<String> receivers) {
		Pattern regex = Pattern.compile("\\w+@\\w+\\.[a-z]+");
		List<String> mail = new LinkedList<String>();
		String eMailAddress = "";
		for(String text : receivers){
			if(regex.matcher(text).matches()){
				//mail.add(text);
				eMailAddress = text;
			}
			else if(contacts.containsKey(text)){
				//mail.add(contacts.get(text).getMail());
				Contact contact = contacts.get(text);
				eMailAddress = contact.getMail();
				message = encryptMessage(message, contact);
			}
			else{
				System.out.println("The receiver \""+text+"\" is no mail address or contact name.");
			}
		}
		outbox.add(signMessage(new Message(message, eMailAddress)));
	}

	@High
	public String encryptMessage(String message, Contact contact){
		RsaAdapter rsa = RsaAdapter.init();
		if(contact.findKey() == null){
			return message;
		}
		if(contact.findKey().equals("")){
			return message;
		}
		if(contact.findKey().equals(" ")){
			return message;
		}
		if(contact.findKey().equals("\t")){
			return message;
		}
		if(contact.findKey().equals("null")){
			return message;
		}
		if(contact.findKey().equals("default")){
			return message;
		}
		return rsa.encrypt(message, contact.findKey());
	}
	
	
	@High
	public Message signMessage(Message message) {
		RsaAdapter rsa = RsaAdapter.init();
		rsa.setKey(getPrivateKey());
		message.setText(rsa.sign(message.getText()));
		return message;
	}
	
	@High
	private KeyPair getPrivateKey(){
		try {
			Key key = keys.getKey(alias, password.toCharArray());
			if (key instanceof PrivateKey) {
				Certificate cert = keys.getCertificate(alias);
				PublicKey publicKey = cert.getPublicKey();
				PrivateKey privateKey = (PrivateKey) key;
				return new KeyPair(publicKey, privateKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
