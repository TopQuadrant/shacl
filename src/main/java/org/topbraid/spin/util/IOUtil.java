package org.topbraid.spin.util;

import java.io.* ;
import java.nio.charset.StandardCharsets ;

public class IOUtil {

	public static StringBuffer loadString(Reader reader) throws IOException {
	    try(BufferedReader bis = new BufferedReader(reader)) {
		    StringBuffer sb = new StringBuffer();
		    for (;;) {
		        int c = bis.read();
		        if (c < 0) {
		            break;
		        }
		        sb.append((char)c);
		    }
		    return sb;
		}
	}

	public static StringBuffer loadStringUTF8(InputStream in) throws IOException {
	    return loadString(new InputStreamReader(in, StandardCharsets.UTF_8));
	}
}
