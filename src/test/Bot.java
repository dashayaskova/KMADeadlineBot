package test;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot{
	
	@Override
	public String getBotUsername() {
		return "KMADeadlineBot";
	}

	@Override
	public void onUpdateReceived(Update update) {
		
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

	@Override
	public String getBotToken() {
		// insert bot api token from telegram chat or trello board
		return "...";
	}
}