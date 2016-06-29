package com.roc.utils.r;


public class Parser {
	
	public static Boolean toBoolean(String strBolean) {
		Boolean bBoolean = null; 
		if (strBolean!=null && strBolean.equalsIgnoreCase("true")) bBoolean = true;
		if (strBolean!=null && strBolean.equalsIgnoreCase("false")) bBoolean = false;
		return bBoolean;
	}
	
}
