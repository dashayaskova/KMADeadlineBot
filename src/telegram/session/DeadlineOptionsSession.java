package telegram.session;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import model.Deadline;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author mike_mars */

public class DeadlineOptionsSession extends Session {

	private Deadline deadline;
	private Community community;
	
	public DeadlineOptionsSession(KMADeadlineBot bot, long userId, Deadline deadline) {
		super(bot, userId);
		this.deadline = deadline;
		this.community = bot.communityDao.select(deadline.getCommunityName());
		sendDeadlineInfo();
	}
	
	private void sendDeadlineInfo() {
		String text = "--- Deadlines ---\n\n"
				+ "Community name: " + deadline.getCommunityName() + "\n\n"
				+ "Deadline description:\n"
				+ deadline.getDescription() + "\n\n"
				+ "Date of deadline: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(deadline.getDate()) + "\n\n"
				+ (community.isMember(userId) ? ("/notifications\n") : "")
				+ (community.isAdmin(userId) ? ("/edit - редагувати дедлайн\n"
						+ "/delete - delete deadline\n") : "")
				+ "/community - \n"
				+ "\n/home";
		bot.sendText(userId, text);
		
		for(long messageId : deadline.getMessageIds().stream().sorted().collect(Collectors.toList())) {
			try {
				bot.execute(new ForwardMessage()
						.setFromChatId(deadline.getChatId())
						.setChatId(userId)
						.setMessageId((Integer) (int) messageId));
			} catch (TelegramApiException e) { e.printStackTrace(); }
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase();
			
			if(community.isMember(userId) && text.equals("/notifications")) {
				return null;//new EditNotificationsSession(bot, userId, deadline);
			} else if(community.isAdmin(userId) && text.equals("/edit")) {
				return null;//new EditDeadlineSession(bot, userid, deadline);
			} else if(community.isAdmin(userId) && text.equals("/delete")) {
				bot.sendText(userId, "Delete ? (/yes | /no)");
				return new Session(bot, userId) {
					public Session updateListener(Update confirmUpdate) {
						if(confirmUpdate.hasMessage() && confirmUpdate.getMessage().hasText()) {
							String text = confirmUpdate.getMessage().getText();
							if(text.equalsIgnoreCase("/yes")) {
								bot.deadlineDao.delete(deadline.getId());
								bot.sendText(userId, "Dlete completed");
								return new DeadlineOptionsSession(bot, userId, deadline);
							} else if(text.equalsIgnoreCase("/no")) {
								bot.sendText(userId, "Delete denied");
								return new DeadlineOptionsSession(bot, userId, deadline);
							}
						}
						
						bot.sendText(userId, "Delete ? (/yes | /no)");
						return this;
					}
				};
			}
			
		}
		
		sendDeadlineInfo();
		return this;
	}

}
