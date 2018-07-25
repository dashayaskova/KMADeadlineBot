package telegram.session;

import java.util.HashSet;
import java.util.Set;

import org.telegram.telegrambots.api.objects.Update;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class CreateCommunitySession extends Session {
	
	public CreateCommunitySession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		sendText();
	}
	
	public void sendText() {
		bot.sendText(userId,
				"--- створення нової спільноти ---\n\n"
				+ "напиши назву нової спільноти : "
				+ "1. містить лише малі літери латинського алфавіту, цифри та знак '_'\n"
				+ "2. починається з літери" + "3. не закінчується знаком '_'\n"
				+ "4. містить від 4 до 64 символів\n\n" + "/home - додому");
	}

	@Override
	public Session updateListener(Update update) {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText();
			
			if(text.length() < 4 || text.length() > 30) {
				bot.sendText(userId, "назва повинна містити від 4 до 64 символів");
			} else if(!text.matches("[a-z0-9_]+")) {
				bot.sendText(userId, "назва повинна містити лише малі літери латинського алфавіту, цифри та знак '_'");
			} else if(!text.matches("[a-z].*")) {
				bot.sendText(userId, "назва повинна починатись з літери");
			} else if(text.endsWith("_")) {
				bot.sendText(userId, "назва не може закінчуватись знаком '_'");
			} else if(bot.communityDao.contains(text)){
				bot.sendText(userId, "вже існує спільнота з такою назвою");
			} else {
				Set<Long> set = new HashSet<>();
				set.add(userId);
				bot.communityDao.create(text, set, set);
				bot.sendText(userId, "--- створено нову спільноту '" + text + "' ---");
				return new CommunityOptionsSession(bot, userId, text);
			}
		}
		
		sendText();
		return this;
	}
}
