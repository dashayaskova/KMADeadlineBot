package telegram.session;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class MenuSession extends Session {
	
	public static final String MENU_TEXT = "--- @KMADeadlineBot ---\n\n"
			+ "- /create_community - створити нову спільноту\n"
			+ "- /search_community - шукати спільноту\n"
			+ "- /my_communities - мої спільноти\n"
			+ "- /my_deadlines - мої дедлайни\n"
			+ "- /create_deadline - створити новий дедлайн";
	
	public MenuSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		sendMenu();
	}
	
	private void sendMenu () {
		SendMessage message = new SendMessage().setChatId(userId).setText(MENU_TEXT);
		try {
			bot.execute(message);
		} catch (TelegramApiException e) { e.printStackTrace(); }
	}
	
	@Override
	public Session updateListener(Update update) {
	//		if (update.hasMessage() && update.getMessage().hasText()) {
	//
	//			String text = update.getMessage().getText();
	//
	//			switch (text) {
	//			case "/create_community":
	//				return new CreateCommunitySession(bot, userId);
	//			case "/search_community":
	//				return new SearchCommunitySession(bot, userId);
	//			case "/my_communities":
	//				return new MyCommunitiesSession(bot, userId);
	//			case "/my_deadlines":
	//				return new MyDeadlinesSession(bot, userId);
	//			case "/create_deadline":
	//				return new CreateDeadlineSession(bot, userId);
	//			}
	//		}
	//		
	//		sendMenu();
		return null;
	}
	
	@Override
	public Session errorListener(Update update) {
		return this;
	}
}
