package example.mailapp;

import java.security.Key;
import java.security.KeyPair;
import java.security.Signature;

import javax.crypto.Cipher;

import org.gravity.security.annotations.requirements.Critical;
import org.gravity.security.annotations.requirements.High;

@Critical
public class RsaAdapter {

	private KeyPair key;
	
	private RsaAdapter(){
		
	}
	
	public static RsaAdapter init() {
		return new RsaAdapter();
	}

	@High
	public String sign(String text) {
		try {
			Signature sig = Signature.getInstance("SHA1WithRSA");
	        sig.initSign(key.getPrivate());
	        sig.update(text.getBytes());
	        return new String(sig.sign());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String encrypt(String text, Key key){
		try{
			 Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			 cipher.init(Cipher.ENCRYPT_MODE,key);
			 cipher.update(text.getBytes());
			 return new String(cipher.doFinal());
		 }catch (Exception e){
			 e.printStackTrace();
		 }
		return null;
	}
	
	@High
	public void setKey(KeyPair key) {
		this.key = key;
	}
	
	public void cleanup(){
		key = null;
	}

}
