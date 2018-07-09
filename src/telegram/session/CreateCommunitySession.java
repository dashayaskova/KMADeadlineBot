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
				"--- створення спільноти ---\n\n"
				+ "нипиши назву нової спільноти, яка:\n"
				+ "1. містить малі літери латинського алфавіту, цифри та знак '_'\n"
				+ "2. починається з малої літери латинського алфавіту\n" + "3. не закінчується знаком '_'\n"
				+ "4. має довжину від 4 до 30 символів\n\n" + "/home - додому");
	}

	@Override
	public Session updateListener(Update update) {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText();
			
			if(text.length() < 4 || text.length() > 30) {
				bot.sendText(userId, "назва повинна містити від 4 до 30 символів");
			} else if(!text.matches("[a-z0-9_]+")) {
				bot.sendText(userId, "назва повинна містити лише літери латинського алфавіту, циври та знак '_'");
			} else if(!text.matches("[a-z].*")) {
				bot.sendText(userId, "назва повинна починатись з літери латинського алфавіту");
			} else if(text.endsWith("_")) {
				bot.sendText(userId, "назва не може закінчуватись знаком '_'");
			} else if(bot.communityDao.contains(text)){
				bot.sendText(userId, "спільнота з такою назвою вже існує");
			} else {
				Set<Long> set = new HashSet<>();
				set.add(userId);
				bot.communityDao.create(text, set, set);
				bot.sendText(userId, "--- створена нова спільнота '" + text + "' ---");
				return new CommunityOptionsSession(bot, userId, text);
			}
		}
		
		sendText();
		return this;
	}
}
