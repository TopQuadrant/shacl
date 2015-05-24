package org.topbraid.spin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;


public class IOUtil {

	public static StringBuffer loadString(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader bis = new BufferedReader(reader);
		for (;;) {
			int c = bis.read();
			if (c < 0) {
				break;
			}
			sb.append((char)c);
		}
		bis.close();
		return sb;
	}

	public static StringBuffer loadStringUTF8(
			InputStream in) throws IOException {
		try {
			return loadString(new InputStreamReader(in,"utf-8"));
		}
		catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

}
