package utils;

public class MyError {
	public static void err(String msg, boolean exit) {
		System.err.println(msg);
		if(exit) {
			System.exit(-1);
		}
	}
	public static void err(String msg) {
		err(msg, true);
	}
}
