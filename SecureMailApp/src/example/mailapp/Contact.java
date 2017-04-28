package example.mailapp;

import java.security.Key;

public class Contact {

	private String name, surname, mail;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public Key getKey(){
		Key key = getKeyFromServer("server1");
		if(key != null){
			return key;
		}
		key = getKeyFromServer("server2");
		if(key != null){
			return key;
		}
		key = getKeyFromServer("server3");
		if(key != null){
			return key;
		}
		key = getKeyFromServer("server4");
		if(key != null){
			return key;
		}
		key = getKeyFromServer("server5");
		if(key != null){
			return key;
		}
		return null;
	}
	
	public Key getKeyFromServer(String url){
		//...........TODO
		return null;
	}
	
}
