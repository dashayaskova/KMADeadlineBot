package telegram.session.api;

import org.telegram.telegrambots.api.objects.Update;

public abstract class Session {

	// unique value
	public final long userId;
	
	// you can check how many times this session was executed
	// there is no auto increment of this value
	public int pointer = 0;

	// constructor
	public Session(long userId) {
		this.userId = userId;
	}

	public abstract void updateListener(Update update);

	// default implementation where errorListener ignore errors
	public void errorListener(Update update) {
		// if you need the error listener - implement this method
	}

	public final void execute(Update update) {
		try {
			updateListener(update);
		} catch (Exception exception) {
			errorListener(update);
		}
	}

	// Override equals and hashCode methods
	// to use Set<Session> in session container

	@Override
	public boolean equals(Object object) {
		// sessions are equals if their user ids are equals
		if (object instanceof Session) {
			Session session = (Session) object;
			return this.userId == session.userId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return String.valueOf(userId).hashCode();
	}
}