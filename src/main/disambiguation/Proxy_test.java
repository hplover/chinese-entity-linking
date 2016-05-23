package main.disambiguation;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Proxy_test {
	public static void main(String args[]) throws IOException{
//		final String authUser = "plover";
//		final String authPassword = "wandadeba";
//
//
//
//		Authenticator.setDefault(
//		               new Authenticator() {
//		                  public PasswordAuthentication getPasswordAuthentication() {
//		                     return new PasswordAuthentication(
//		                           authUser, authPassword.toCharArray());
//		                  }
//		               }
//		            );
		System.setProperty("http.proxyHost", "123.57.133.112");
		System.setProperty("http.proxyPort", "8080");
		Document doc = Jsoup.connect("http://people.cs.umass.edu/~vdang/ranklib.html").get();
		System.out.println(doc);
	}
}
