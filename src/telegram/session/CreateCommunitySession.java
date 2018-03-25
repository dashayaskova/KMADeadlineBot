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
<<<<<<< HEAD
		// TODO Auto-generated method stub

=======
		System.out.println("hello");
		
>>>>>>> a54948bb1686a29d640b4a55e7d8fb136d2f3b5d
	}

}
