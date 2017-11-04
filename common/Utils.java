package common;

import java.net.InetSocketAddress;

public class Utils {
	public static InetSocketAddress parseSocketAddress(String addrPort) {
		try {
			String splits[] = addrPort.trim().split(":");
			if (splits.length != 2) {
				throw new Exception();
			}
			return new InetSocketAddress(splits[0], Integer.valueOf(splits[1]));
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid net address [" + addrPort + "]");
		}
	}
	
	public static void require(boolean satified, String error) {
		if (!satified)
			throw new RuntimeException(error);
	}
	
	public static void require(boolean satified) {
		if (!satified)
			throw new RuntimeException();
	}
}
