package helper;

public class Misc {
	public static boolean IsNullOrEmpty(String str){
		if(str==null){
			return true;
		}
		
		return str.isEmpty();
	}
	
	public static boolean IsNullOrWhiteSpace(String str){
		if(str==null){
			return true;
		}
		
		return str.trim().isEmpty();
	}
}
