package telegram.session;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class EditAdminListSession extends Session {
	
	private Community community;
	
	public EditAdminListSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
	}
	
	public EditAdminListSession(KMADeadlineBot bot, long userId, String communityName) {
		this(bot, userId, bot.communityDao.select(communityName));
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
