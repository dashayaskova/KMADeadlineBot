package telegram.session;

import org.telegram.telegrambots.api.objects.Update;

public class Session {

	// unique value
	private final long userId;

	// listeners with default values
	private UpdateListener updateListener = (update) -> {};
	private UpdateListener errorListener = (update) -> {};

	// you can check how many times this session was executed
	private int pointer = 0;

	public int getPointer() {
		return pointer;
	}
	
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}
	
	// constructor with default update and error listeners
	public Session(long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return userId;
	}

	// update listener setter
	public Session onUpdate(UpdateListener listener) {
		updateListener = listener;
		return this;
	}

	// error listener setter
	public Session onError(UpdateListener listener) {
		errorListener = listener;
		return this;
	}

	public void execute(Update update) {
		try {
			updateListener.execute(update);
		} catch (Exception exception) {
			errorListener.execute(update);
		}
	}
	
	
	// Override equals and hashCode methods
	// to use Set<Session> in session container
	
	@Override
	public boolean equals(Object object) {
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