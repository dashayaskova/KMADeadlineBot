package telegram.bot;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class Bot extends TelegramLongPollingBot{

	@Override
	public String getBotUsername() {
		return "KMADeadlineBot";
	}

	@Override
	public void onUpdateReceived(Update update) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBotToken() {
		// insert bot api token from telegram chat or trello board
		return "";
	}
}
