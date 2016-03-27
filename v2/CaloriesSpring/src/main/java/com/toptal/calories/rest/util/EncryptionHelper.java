package com.toptal.calories.rest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptionHelper 
{
	private static final Logger log = LoggerFactory.getLogger(EncryptionHelper.class);
	
	public EncryptionHelper() 
	{
	}
	
	public String encrypt(String text) 
	{
		String encrypted = text;
		
		MessageDigest sha256 = null;
		try {
			sha256 = MessageDigest.getInstance("SHA-256");
		    byte[] passBytes = text.getBytes();
		    byte[] passHash = sha256.digest(passBytes);
		    encrypted = DatatypeConverter.printBase64Binary(passHash);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			log.error("Encrypt Error: " , e);
		}        
	    
	    return encrypted;
	}

	public static void main(String[] args) 
	{
		if (args.length == 0) {
			log.error("Please specify a string to encrypt");
			System.exit(1);
		}
		EncryptionHelper helper = new EncryptionHelper();
		String encrypted = helper.encrypt(args[0]);
		
		log.info("Encrypted: " + encrypted);
	}
}
