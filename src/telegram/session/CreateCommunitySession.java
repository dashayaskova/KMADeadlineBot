package telegram.session;

import org.telegram.telegrambots.api.objects.Update;

import telegram.session.api.Session;

public class CreateCommunitySession extends Session{

	public CreateCommunitySession(long userId) {
		super(userId);
		// TODO bot should send message that session is started
	}

	@Override
	public void updateListener(Update update) {
		System.out.println("hello");
		
	}

}
