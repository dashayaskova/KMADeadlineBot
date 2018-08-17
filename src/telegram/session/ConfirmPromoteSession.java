package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class ConfirmPromoteSession extends Session {
	private Community community;

	public ConfirmPromoteSession(KMADeadlineBot bot, long userId, Community community, Set<Long> promoted) {
		super(bot, userId);
		this.community = community;
		promoted.forEach(id -> community.addAdmin(id));
		bot.communityDao.update(community);
		StringBuffer text = new StringBuffer();
		text.append("<i>Operation succsessful</i>\nThese people are now admins:");
		try {
			for (long id : promoted) {
				String username = bot.execute(new GetChat(id)).getUserName();
				text.append("\n@"+username);
			}
			text.append("\n\n/community\n/home");
			bot.sendText(userId, text.toString());
		} catch (TelegramApiException e) {
			bot.sendText(userId, "<i>Operation failed</i>\nSomething went wrong.\n/community\n/home");
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().getText().equals("/community")) {
			return new CommunityOptionsSession(bot, userId, community);
		}
		return new MenuSession(bot, userId);
	}

}
