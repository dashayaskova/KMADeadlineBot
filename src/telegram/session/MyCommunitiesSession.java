package telegram.session;

import java.util.Set;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class MyCommunitiesSession extends Session {
	
	private Set<String> communityNames;

	public MyCommunitiesSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		communityNames = bot.communityDao.selectNamesByMemberId(userId);
		sendMyCommunities();
	}
	
	public void sendMyCommunities() {
		String text = "--- мої спільноти ---\n"
				+ communityNames.stream().sorted().collect(Collectors.joining("\n-", "\n-", "\n\n"))
				+ "щоб обрати спільноту напиши її назву:\n\n/home";
		
		if(communityNames.size() == 0) {
			text = "--- мої спільноти ---\n\nсписок спільнот порожній\n\n/home";
		}
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
