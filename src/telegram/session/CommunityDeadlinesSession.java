package telegram.session;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import model.Deadline;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class CommunityDeadlinesSession extends Session {
	
	public static final int DEADLINES_ON_PAGE = 6;

	private Community community;
	private int page = 0;
	private List<Deadline> deadlines;
	private String messageText;
	
	public CommunityDeadlinesSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
		sendDeadlineList();
	}

	public CommunityDeadlinesSession(KMADeadlineBot bot, long userId, String communityName) {
		this(bot, userId, bot.communityDao.select(communityName));
	}
	
	private void sendDeadlineList() {
		messageText = "--- дадлайни спільноти '" + community.getName() + "' ---\n\n";
		
		deadlines = bot.deadlineDao.selectForCommunity(community.getName())
				.stream().sorted().collect(Collectors.toList());
		
		int deadlinesOnCurrentPage = 0;
		
		if(!deadlines.isEmpty()) {

			for (int i = page * DEADLINES_ON_PAGE; i < page * DEADLINES_ON_PAGE + DEADLINES_ON_PAGE
					&& i < deadlines.size(); i++, deadlinesOnCurrentPage++) {

				Deadline deadline = deadlines.get(i);
				
				int index = i - page * DEADLINES_ON_PAGE + 1;
				String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(deadline.getDate());
				String shortDescription = deadline.getDescription().length() >= 128
						? deadline.getDescription().substring(0, 128).trim().concat("...")
						: deadline.getDescription();
						
				messageText += "/" + index + " " + date + "\n"
						+ "   " + shortDescription + "\n\n";

			}
	
		} else {
			messageText += "список дедлайнів порожній\n\n";
		}
		
		messageText += "/community - інформація про спільноту\n";
		
		if(community.isAdmin(userId)) {
			messageText += "/edit - редагувати список дедлайнів\n"
					+ "/create - створити новий дедлайн\n";
		}
		
		messageText += "\n/home - додому";
		
		InlineKeyboardBuilder builder = InlineKeyboardBuilder.create(userId).setText(messageText);
		
		for(int i = 1; i <= deadlinesOnCurrentPage; i++) {
			builder.addButton(String.valueOf(i), String.valueOf(i));
		}
		
		SendMessage message = builder.nextRow()
				.addButton("<-", "<-")
				.addButton("->", "->").nextRow().build();

		try {
			bot.execute(message);
		} catch (TelegramApiException e) { e.printStackTrace(); }
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText();
			
			if(text.equalsIgnoreCase("/community")) {
				return new CommunityOptionsSession(bot, userId, community.getName());
			
			} else if (text.equalsIgnoreCase("/edit") && community.isAdmin(userId)) {
				// return new EditCommunityDeadlinesSession(bot, userId, community.getName());
				
			} else if (text.equalsIgnoreCase("/create") && community.isAdmin(userId)) {
				return new CreateDeadlineSession(bot, userId);
				// return new CreateDeadlineSession(bot, userId, community.getName());
			
			} else if (text.matches("/[1-6]")) {
				int index = page * DEADLINES_ON_PAGE + Integer.parseInt(text.substring(1)) - 1;
				if(index < deadlines.size()) {
					EditMessageText editMessage = new EditMessageText().setChatId(userId)
							.setMessageId(update.getMessage().getMessageId() - 1)
							.setText(messageText);
					bot.execute(editMessage);
					return new DeadlineOptionsSession(bot, userId, deadlines.get(index));
				} else {
					bot.sendText(userId, "такого дедлайну не існує");
				}
				
			} else {
				bot.sendText(userId, "недоступна команда");
				sendDeadlineList();
			}
			
		} else if (update.hasCallbackQuery()) {
			
			String query = update.getCallbackQuery().getData();
			
			if(query.equals("<-") && page > 0) {
				System.out.println("back");
				DeleteMessage deleteMessage = new DeleteMessage().setChatId(String.valueOf(userId))
						.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
				bot.execute(deleteMessage);
				page--;
				sendDeadlineList();
				
			} else if (query.equals("->") && deadlines.size() > (page + 1) * DEADLINES_ON_PAGE) {
				System.out.println("next");
				DeleteMessage deleteMessage = new DeleteMessage().setChatId(String.valueOf(userId))
						.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
				bot.execute(deleteMessage);
				page++;
				sendDeadlineList();
				
			} else if (query.matches("[1-6]")) {
				int index = page * DEADLINES_ON_PAGE + Integer.parseInt(query) - 1;
				EditMessageText editMessage = new EditMessageText().setChatId(userId)
						.setMessageId(update.getCallbackQuery().getMessage().getMessageId() - 1)
						.setText(messageText);
				bot.execute(editMessage);
				return new DeadlineOptionsSession(bot, userId, deadlines.get(index));
			}
		}
		
		return this;
	}
	
	@Override
	public Session errorListener(Update update) {
		bot.sendText(userId, "помилка");
		return new CommunityDeadlinesSession(bot, userId, community.getName());
	}
}
