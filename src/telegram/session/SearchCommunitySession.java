package telegram.session;

import java.util.Set;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class SearchCommunitySession extends Session {
	
	private Set<String> communities;
	
	public SearchCommunitySession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		communities = bot.communityDao.selectNames();
		sendCommunities();
	}
	
	private void sendCommunities() {
		String text = communities.stream()
				.sorted()
				.limit(100)
				.map(name -> "-" + name)
				.collect(Collectors.joining("\n"));
		text += "\n...\n enter community name";
		
		SendMessage message = new SendMessage()
				.setChatId(userId)
				.setText(text);
		
		try {
			bot.execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase();
			
			String communityName = communities.stream().filter(name -> name.toLowerCase().startsWith(text))
					.sorted().findFirst().orElse(null);
			
			if(communityName == null) {
				sendCommunities();
				return this;
				
			} else {
				return new JoinCommunitySession(bot, userId, communityName);
			}
		}
		
		sendCommunities();
		return this;
	}

}
