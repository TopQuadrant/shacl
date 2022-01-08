package org.topbraid.jenax.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class BasicAuthenticator extends Authenticator {
	private final String userName;
	private final char[] password;


	public static Authenticator with(String userName) {
		return with(userName, (char[]) null);
	}

	public static Authenticator with(String userName, String password) {
		return with(userName, password.toCharArray());
	}

	public static Authenticator with(String userName, char[] password) {
		return new BasicAuthenticator(userName, password);
	}

	private BasicAuthenticator(String userName, char[] password) {
		super();
		this.userName = userName;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.userName, this.password);
	}
}
