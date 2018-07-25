package telegram.session.api;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;

public abstract class Session {

	// unique value
	public final long userId;
	
	// bot
	// use to send messages
	public final KMADeadlineBot bot;
	
	//	// you can check how many times this session was executed
	//	// there is no auto increment of this value
	//	public int pointer = 0;

	// constructor
	public Session(KMADeadlineBot bot, long userId) {
		this.userId = userId;
		this.bot = bot;
	}

	// you shouldn't catch telegram exceptions,
	// if they will be thrown method errorListener will catch them
	// returns next session
	public abstract Session updateListener(Update update) throws TelegramApiException ;

	// default implementation where errorListener ignore errors
	// returns next session
	public Session errorListener(Update update) {
		// if you need the error listener - implement this method
		return null;
	}

	public final void process(Update update) {
		Session nextSession;
		try {
			nextSession = updateListener(update);
		} catch (Exception exception) {
			exception.printStackTrace();
			nextSession = errorListener(update);
		}
		
		// if has next session, add it to SessionContainer
		if(nextSession != null) {
			bot.sessionContainer.add(nextSession);
		} else {
		// if doesn't have, remove this session from SessoinContainer
			bot.sessionContainer.remove(userId);
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
	
	// create Session without extending Session class, but using lambda expressions
	public static interface UpdateListener { Session updateListener(Update update) throws TelegramApiException; }
	public static interface ErrorListener { Session errorListener(Update update); }
	
	public static Session create(KMADeadlineBot bot, long userId, UpdateListener updateListener) {
		return create(bot, userId, updateListener, (update) -> null);
	}
	
	public static Session create(KMADeadlineBot bot, long userId, UpdateListener updateListener, ErrorListener errorListener) {
		return new Session(bot, userId) {
			public Session updateListener(Update update) throws TelegramApiException{
				return updateListener.updateListener(update);
			}
			public Session errorListener(Update update) {
				return errorListener.errorListener(update);
			}
		};
	}
}