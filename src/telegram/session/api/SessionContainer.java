package telegram.session.api;

import java.util.HashSet;
import java.util.Set;

public class SessionContainer {

	// opened session list
	private static Set<Session> sessions = new HashSet<>();

	public static void add(Session session) {
		if (contains(session.userId)) {
			remove(session.userId);
		}
		sessions.add(session);
	}
	
	public static void remove(long userId) {
		if (contains(userId)) {
			Session session = get(userId);
			sessions.remove(session);
		}
	}

	public static boolean contains(long userId) {
		return sessions.stream().anyMatch((session) -> session.userId == userId);
	}

	public static Session get(long userId) {
		if (contains(userId)) {
			return sessions.stream().filter((session) -> session.userId == userId).findAny().get();
		}
		return null;
	}
}