package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class MyAdminSession extends Session {

	public MyAdminSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		StringBuffer text = new StringBuffer();
		text.append("The list of communities, wehre you are an admin:\n");
		Set<String> names = bot.communityDao.selectNamesByAdminId(userId);
		for(String name : names) {
			text.append("/_" +name+"\n");
		}
		text.append("\nYou can select community, by clicking on it. To return click: /my_communities or /home");
		bot.sendText(userId, text.toString());
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage()) {
			if(update.getMessage().getText().startsWith("/_")) {
				bot.sessionContainer.remove(userId);
				return new CommunityOptionsSession(bot, userId, update.getMessage().getText().substring(2));
			} else if(update.getMessage().getText().equals("/home")) {
				bot.sessionContainer.remove(userId);
				return new MenuSession(bot, userId);
			} else if(update.getMessage().getText().equals("/my_communities")) {
				bot.sessionContainer.remove(userId);
				return new MyCommunitiesSession(bot, userId);
			}
		}
		return null;
	}
}
