package telegram.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.session.api.SessionContainer;

public class KMADeadlineBot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() {
		return "KMADeadlineBot";
	}

	@Override
	public String getBotToken() {
		return "telegram bot api token";
	}

	// update listener

	@Override
	public void onUpdateReceived(Update update) {
		long userId = getUserId(update);

		if (SessionContainer.contains(userId)) {

			// if contains opened session with this user, continue it
			SessionContainer.get(userId).execute(update);

		} else if (update.getMessage() != null && update.getMessage().getText() != null) {

			// if contains message, parse it and find commands
			String text = update.getMessage().getText();

			if ("/start".equals(text)) {
				// if /start
			} else if ("/help".equals(text)) {
				// if /help
			} else if ("/all_deadlines".equals(text)) {
				// if /all_deadlines
			}

		} else if (update.getCallbackQuery() != null) {

			// if contains callback query, check it
			// ...

		}
	}

	public void sendText(long receiverId, String message) {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(receiverId);
		sendMessage.setText(message);

		try {
			execute(sendMessage);
		} catch (TelegramApiException exception) {
			exception.printStackTrace();
		}
	}

	// static methods 
	
	public static long getUserId(Update update) {
		if (update.getMessage() != null) {
			// if this update contains message, get id from message
			return update.getMessage().getFrom().getId();
		} else {
			// else get id from callback quary
			return update.getCallbackQuery().getFrom().getId();
		}
	}
}
