package telegram.session;

import java.util.HashSet;
import java.util.Set;

import org.telegram.telegrambots.api.objects.Update;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class CreateCommunitySession extends Session {
	
	public CreateCommunitySession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		sendText();
	}
	
	public void sendText() {
		bot.sendText(userId,
				"--- creating community ---\n\n"
				+ "Write name of new community: "
				+ "1. it may have only latin letters and '_'\n"
				+ "2. Starts with small letter" + "3. Doesn't end with '_'\n"
				+ "4. is from 4 to 30 characters in length \n\n" + "/home - home");
	}

	@Override
	public Session updateListener(Update update) {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText();
			
			if(text.equals("/home")) {
				bot.sessionContainer.remove(userId);
				bot.sessionContainer.add(new MenuSession(bot, userId));
				return null;
			}
			if(text.length() < 4 || text.length() > 30) {
				bot.sendText(userId, "Name must be from 4 to 30 chars");
			} else if(!text.matches("[a-z0-9_]+")) {
				bot.sendText(userId, "Only latin letters and '_'");
			} else if(!text.matches("[a-z].*")) {
				bot.sendText(userId, "Must start with small lastin letter");
			} else if(text.endsWith("_")) {
				bot.sendText(userId, "Cannot end with '_'");
			} else if(bot.communityDao.contains(text)){
				bot.sendText(userId, "there's community with this name");
			} else {
				Set<Long> set = new HashSet<>();
				set.add(userId);
				bot.communityDao.create(text, set, set);
				bot.sendText(userId, "--- you've created new community" + text + "' ---");
				return new CommunityOptionsSession(bot, userId, text);
			}
		}
		
		sendText();
		return this;
	}
}
