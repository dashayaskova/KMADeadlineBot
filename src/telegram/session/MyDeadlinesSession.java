package telegram.session;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class MyDeadlinesSession extends Session {

	public MyDeadlinesSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

}
