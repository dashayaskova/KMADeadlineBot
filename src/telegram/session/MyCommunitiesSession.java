package telegram.session;

import java.util.List;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class MyCommunitiesSession extends Session {
	
	private List<String> communityNames;

	public MyCommunitiesSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		communityNames = bot.communityDao.selectNamesByMemberId(userId).stream().sorted().collect(Collectors.toList());
		sendMyCommunities();
	}
	
	public void sendMyCommunities() {
		String text = "--- мої спільноти ---\n\n";
				
		
		
		
		if(communityNames.isEmpty()) {
			text += "список спільнот порожній\n"
					+ "/search_community - знайти спільноту\n"
					+ "/create_community - створити нову спільноту\n\n";
		} else {
			
			for(int i = 0; i < communityNames.size(); i++) {
				text += "/" + (i + 1) + " " + communityNames.get(i) + "\n";
			}
			
		}
		
		text += "/home - додому";
		bot.sendText(userId, text);
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase();
			
			if(text.equalsIgnoreCase("/home")) {
				return new MenuSession(bot, userId);
			}
			
			String communityName = communityNames.stream()
					.filter(name -> name.toLowerCase().startsWith(text))
					.sorted()
					.findFirst().orElse(null);
			if(communityName != null) {
				return new CommunityOptionsSession(bot, userId, communityName);
			} else {
				bot.sendText(userId, "no matches");
			}
		}
		
		sendMyCommunities();
		return null;
	}

}
