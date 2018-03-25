package telegram.session;

import org.telegram.telegrambots.api.objects.Update;

import telegram.session.api.Session;
import telegram.bot.Bot;

public class CountSession extends Session {

	public CountSession(Bot bot, long userId) {
		super(userId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateListener(Update update) {
		// TODO Auto-generated method stub
		
	}

}
