package telegram.session;

import org.telegram.telegrambots.api.objects.Update;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class CreateCommunitySession extends Session {

	public CreateCommunitySession(KMADeadlineBot bot, long userId) {
		super(userId);
		// TODO bot should send message that session is started
	}

	@Override
	public void updateListener(Update update) {
		// TODO Auto-generated method stub

	}

}
