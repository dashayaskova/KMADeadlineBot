package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author dSigma */

public class ConfirmEditAdminListSession extends Session {
	private Community community;
	
	public ConfirmEditAdminListSession(KMADeadlineBot bot, long userId, Community community, Set<Long> deleted) {
		super(bot, userId);
		this.community = community;
		Set<Long> admins = community.getAdminIds();
		if(admins.equals(deleted)) {
			bot.sendText(userId, "<i>Operation failed</i>\nYou cannot delete all admins at once! \n/community\n/home");
		} else {
			for(long id : deleted) {
				community.removeAdminId(id);
			}
			bot.communityDao.update(community);
			bot.sendText(userId, "<i>Operation succsessful</i>\n/community\n/home");
		}
		
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().getText().equals("/community")) {
			return new CommunityOptionsSession(bot, userId, community);
		}
		
		bot.sendMenuMessage(userId);
		return null;
	}

}
