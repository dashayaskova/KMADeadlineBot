package telegram.session;

import java.util.HashSet;
import java.util.Set;

public class SessionContainer {

	// opened session list
	private static Set<Session> sessions = new HashSet<>();

	public static void add(Session session) {
		if (contains(session.getUserId())) {
			remove(session.getUserId());
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
		return sessions.stream().anyMatch((session) -> session.getUserId() == userId);
	}

	public static Session get(long userId) {
		if (contains(userId)) {
			return sessions.stream().filter((session) -> session.getUserId() == userId).findAny().get();
		}
		return null;
	}

	public static Session create(long userId) {
		Session session = new Session(userId);
		remove(session.getUserId());
		add(session);
		return session;
	}
}