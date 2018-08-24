package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class MyCommunitiesSessionDasha extends Session{
	
	private Set<String> communityNames;
	
	public  MyCommunitiesSessionDasha(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		StringBuffer text = new StringBuffer();
		text.append("The list of communities, where you are an member:\n");
		
		communityNames = bot.communityDao.selectNamesByMemberId(userId);
		for(String name : communityNames) {
			text.append("/_" +name+"\n");
		}
		
		text.append("\nYou can select community, by clicking on it. To return click: /my_communities or /home");
		bot.sendText(userId, text.toString());
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage()) {
			if(update.getMessage().getText().startsWith("/_")) {
				String text = update.getMessage().getText();
				if(communityNames.contains(text.substring(2))) {
					return new CommunityOptionsSession(bot, userId, update.getMessage().getText().substring(2));

				}
			}
		}
		
		bot.sendText(userId, "choose community name which is in the list");
		return this;
	}
}
