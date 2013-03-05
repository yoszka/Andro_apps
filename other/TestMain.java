package com.example.tests;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {
	static int mAaa;
	static String[] mStr;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] outcomeArray = null;
		String str1 = "_ID IN (12,123,4568)";
		String str2 = "_ID IN (12)";
		String[] str;
		int bbb;
		
//		System.out.println("["+str+"]");	// Cannot be compiled: The local variable str may not have been initialized
//		System.out.println("["+bbb+"]");	// Cannot be compiled: The local variable bbb may not have been initialized
		System.out.println("TestMain mAaa=["+mAaa+"]");	// isn't local variable so it is initialized by default value = 0    (specificator static doesn't play role in this example)
		System.out.println("TestMain mStr=["+mStr+"]");	// isn't local variable so it is initialized by default value = null (specificator static doesn't play role in this example)
		new Klasa2();						// in constructor print also non local (class field) variable initialized by default value = null
		new TestMain().new InnerClass();
		
		
		System.out.println(str1.replaceAll(".*((_ID)\\s*(IN)\\s*\\({1})(.*)(\\)).*", "$4"));
		System.out.println(str2.replaceAll(".*((_ID)\\s*(IN)\\s*\\({1})(.*)(\\)).*", "$4"));
		
		System.out.println("--------------");
		outcomeArray = extractIdToUpdate(str1);
		if(outcomeArray != null){
			for(String id : outcomeArray){
				System.out.println(id);
			}
		}
		
		System.out.println("--------------");
		outcomeArray = extractIdToUpdate(str2);
		if(outcomeArray != null){
			for(String id : outcomeArray){
				System.out.println(id);
			}
		}
		
		System.out.println("--------------");
		SelectionResolver("is_read IS NOT NULL AND is_read = 0 AND date < ?", new String[]{"2013"});
		SelectionResolver("is_read IS NOT NULL AND is_read=0 AND date > ?", new String[]{"2013"});
		SelectionResolver("is_read IS NOT NULL AND is_read=? AND date = ?", new String[]{"2", "2013"});
		SelectionResolver("is_read IS NOT NULL AND new=? AND date > ? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new= ? AND date < ? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new = ? AND date < ? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new = ? AND date=? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new = ? AND date= ? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new = ? AND date > ? ?", new String[]{"1", "2013", "198"});
		SelectionResolver("is_read IS NOT NULL AND new = ? AND new > ? ?", new String[]{"1", "2013", "198"});

		SelectionResolver("NOT (is_read IS NOT NULL AND is_read = 0 AND date > ?)", new String[]{"2013"});
		SelectionResolver("NOT (is_read = 0 AND date= ?)", new String[]{"2013"});
		SelectionResolver("NOT (new IS NOT NULL AND new = 0 AND date <?)", new String[]{"2013"});
		
		
		SelectionResolver("new = 1", null);
		SelectionResolver("new=1", null);
		SelectionResolver("new= 1", null);
		SelectionResolver("is_read = 1", null);
		SelectionResolver("is_read= 2", null);
		SelectionResolver("is_read=0", null);
		SelectionResolver("NOT(is_read=0)", null);
		SelectionResolver("NOT( is_read=0 ) AND new = 4", null);

		SelectionResolver("NOT( type=0 ) AND new = 4", null);
		SelectionResolver("NOT( new=0 ) AND type = 4", null);

		SelectionResolver("new = 1 AND type = ?", new String[]{"1"});
		SelectionResolver("new = 1 AND type = ?", new String[]{"2"});
		
		SelectionResolver("is_read = 0 AND type = 3", new String[]{null});
	}
	
	static String[] extractIdToUpdate(String inputString){
		final String pattern = ".*((_ID)\\s*(IN)\\s*\\({1})(.*)(\\)).*";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(inputString);
		
		// Check if pattern matched at all, then parse it
		if(m.find()){
			String subStringWithIds = (String) inputString.replaceAll(pattern, "$4");
			return subStringWithIds.split(",");
		}

		return null;
	}	
	
	
	private class InnerClass{
		String[] mInnerStr;
		
		InnerClass(){
			String[] innerStr;
			System.out.println("InnerClass mInnerStr=["+mInnerStr+"]");	// isn't local variable so it is initialized by default value = null
//			System.out.println("["+innerStr+"]");		// Cannot be compiled: The local variable innerStr may not have been initialized
		}
	}
	
	private static void SelectionResolver(String selection, String[] selectionArgs){
		Map.Entry<Boolean, String> entry;
		ParameterPair parameterPair = null;
		boolean exist_is_read = false;
		String value_is_read = null;
		boolean exist_new = false;
		String value_new = null;
		boolean exist_type = false;
		String value_type = null;
		boolean exist_date = false;
		String value_date = null;
		String parameter_date = null;
		
		// Merge selection and selectionArgs
		if((selection != null) && (selectionArgs != null)){
			selection = selection.replaceAll("\\?", "%s");
			
			switch(selectionArgs.length){
			case 1:
				selection = String.format(selection, selectionArgs[0]);
				break;
			case 2:
				selection = String.format(selection, selectionArgs[0], selectionArgs[1]);
				break;
			case 3:
				selection = String.format(selection, selectionArgs[0], selectionArgs[1], selectionArgs[2]);
				break;
			case 4:
				selection = String.format(selection, selectionArgs[0], selectionArgs[1], selectionArgs[2], selectionArgs[3]);
				break;
			}
		}
		System.out.println("MERGED: "+selection);
		
		entry = getValuePairEqual(selection, "is_read");
		exist_is_read = entry.getKey();
		value_is_read = entry.getValue();
		
		entry = getValuePairEqual(selection, "new");
		exist_new = entry.getKey();
		value_new = entry.getValue();	
		
		entry = getValuePairEqual(selection, "type");
		exist_type = entry.getKey();
		value_type = entry.getValue();
		
		parameterPair = getValuePairParameter(selection, "date");
		exist_date = parameterPair.isKey_exist();
		value_date = parameterPair.getValue();
		parameter_date = parameterPair.getParameter();
		
		System.out.println("PARSED: " + ((exist_is_read)? "READ" 
				+ ", value_is_read = " + value_is_read 
				+ ", " + (isParameterNegated(selection, "is_read")?"NEGATED ### ":" ### "): "")
				
				+ ((exist_new)? " NEW" 
				+ ", value_new = " + value_new 
				+ ", " + (isParameterNegated(selection, "new")?"NEGATED ### ":" ### "): "")
				
				+ ((exist_type)? " TYPE" 
				+ ", value_type = " + value_type 
				+ ", " + (isParameterNegated(selection, "type")?"NEGATED ### ":" ### "): "")
				
				+ ((exist_date)? " DATE" 
				+ ", value_date = " + value_date 
				+ ", parameter_date: " + parameter_date
				+ ", "+ (isParameterNegated(selection, "date")?"NEGATED ### ":" ### "): ""));
	}
	
	static class ParameterPair{
		final boolean key_exist;
		final String value;
		final String parameter;
		
		public ParameterPair(boolean key_exist, String value, String parameter) {
			this.key_exist = key_exist;
			this.value = value;
			this.parameter = parameter;
		}
		
		public boolean isKey_exist() {
			return key_exist;
		}

		public String getValue() {
			return value;
		}

		public String getParameter() {
			return parameter;
		}
	}
	
	
	static Map.Entry<Boolean, String> getValuePairEqual(String inputText, String key){
		boolean exist_key = false;
		String value = null;
		
		if(inputText != null){
			final String pattern = ".*(("+key+")\\s*[=]\\s*\\d+).*";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(inputText);
			
			// Check if pattern matched at all, then parse it
			if(m.find()){
				exist_key = true;
				String substringIsRead = (String) inputText.replaceAll(pattern, "$1");
				value = (String) substringIsRead.replaceAll("[^0-9]", "");
			}
		}
		
//		Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("Not Unique key1","1");
		return new AbstractMap.SimpleEntry<>(exist_key, value);
	}
	
	static ParameterPair getValuePairParameter(String inputText, String key){
		boolean exist_key = false;
		String value = null;
		String parameter = null;
		
		if(inputText != null){
			final String pattern = ".*(("+key+")\\s*[<=>]\\s*\\d+).*";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(inputText);
			
			// Check if pattern matched at all, then parse it
			if(m.find()){
				exist_key = true;
				String substringIsRead = (String) inputText.replaceAll(pattern, "$1");
				value = (String) substringIsRead.replaceAll("[^0-9]", "");
				parameter = (String) substringIsRead.replaceAll("\\w", "");
			}
		}
		
//		Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("Not Unique key1","1");
		return new ParameterPair(exist_key, value, parameter);
	}
	
	static boolean isParameterNegated(String inputText, String key){
		// NOT (is_read IS NOT NULL AND is_read = 0 AND date > ?)
		
		if(inputText != null){
			final String pattern = ".*(NOT)\\s*(\\()(.*("+key+")\\s*[<=>]\\s*\\d+).*\\).*";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(inputText);
			
			return m.find();
		}
		
		return false;
	}

}
