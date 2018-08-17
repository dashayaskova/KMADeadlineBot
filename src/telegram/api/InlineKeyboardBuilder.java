package telegram.api;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class InlineKeyboardBuilder {

	private Long chatId;
	private String text;

	private List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
	private List<InlineKeyboardButton> row = new ArrayList<>();

	private InlineKeyboardBuilder() {
	}

	public static InlineKeyboardBuilder create(Long chatId) {
		InlineKeyboardBuilder builder = new InlineKeyboardBuilder();
		builder.chatId = chatId;
		return builder;
	}

	public InlineKeyboardBuilder setText(String text) {
		this.text = text;
		return this;
	}

	public InlineKeyboardBuilder nextRow() {
		keyboard.add(row);
		this.row = new ArrayList<>();
		return this;
	}

	public InlineKeyboardBuilder addButton(String text, String callbackData) {
		row.add(new InlineKeyboardButton().setText(text).setCallbackData(callbackData));
		return this;
	}

	public SendMessage build() {
		SendMessage message = new SendMessage();

		message.setChatId(chatId);
		message.setText(text);

		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

		keyboardMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(keyboardMarkup);

		return message;
	}

	public EditMessageText buildEdit() {
		EditMessageText message = new EditMessageText();

		message.setChatId(chatId);
		message.setText(text);

		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

		keyboardMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(keyboardMarkup);

		return message;
	}
	
	public InlineKeyboardMarkup getReplyMarkup() {
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
		keyboardMarkup.setKeyboard(keyboard);
		return keyboardMarkup;
	}

}