package telegram.session;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Deadline;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class EditDeadlineSession extends Session{
	
	private Deadline deadline;
	private Integer messageId;
	private EditDeadlineSession editDeadlineSession;
	
	public EditDeadlineSession(KMADeadlineBot bot, long userId, Deadline deadline) {
		super(bot, userId);
		this.deadline = deadline;
		editDeadlineSession = this;
	}
	
	public EditDeadlineSession(KMADeadlineBot bot, long userId, long deadlineId) {
		this(bot, userId, bot.deadlineDao.select(deadlineId));
	}
	
	private void sendEditText() {
		String text = "--- редагування дедлайну ---";
		SendMessage sendMessage = InlineKeyboardBuilder.create(userId).setText(text)
				.addButton("дата", "date")
				.addButton("опис", "description")
				.addButton("додаткові повідомлення", "messages")
				.nextRow()
				.addButton("зберегти", "save").nextRow().build();
		try {
			messageId = bot.execute(sendMessage).getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Session updateListener(Update update) throws TelegramApiException{
		if(update.hasCallbackQuery()) {
			String query = update.getCallbackQuery().getData();
			if(query.equals("date")) {
				
				EditMessageText editMessage = new EditMessageText()
						.setChatId(userId)
						.setMessageId(messageId)
						.setText("__введи нову дату дедлайну у форматі 'ДД-MM-РРРР ГГ:ХХ:СС'__")
						.setParseMode("markdown");
				
				bot.execute(editMessage);
				
				return new Session(bot, userId) {
					public Session updateListener(Update dateUpdate) {
						if(dateUpdate.hasMessage() && dateUpdate.getMessage().hasText()) {
							String text = dateUpdate.getMessage().getText();
							
							try {
								// date format
								DateFormat parser = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
								Date inputDate = parser.parse(text);

								// if old date
								if (inputDate.getTime() <= System.currentTimeMillis()) {
									bot.sendText(userId, "не можливо створити дедлайн з датою, що вже минула");
									
								} else {
									deadline.setDate(inputDate);
									bot.sendText(userId, "__дату дедлайну змінено__");
									sendEditText();
									return editDeadlineSession;
								}
							} catch (ParseException e) {
								e.printStackTrace();
								bot.sendText(userId, "дата не відповідає формату 'ДД.MM.РРРР ГГ:ХХ:СС'");
								
							}
						}
						return this;
					}
				};
			} else if (query.equals("description")) {
				
				EditMessageText editMessage = new EditMessageText()
						.setChatId(userId)
						.setMessageId(messageId)
						.setText("__введи новий опис дедлайну__")
						.setParseMode("markdown");
				
				bot.execute(editMessage);
				
				return new Session(bot, userId) {
					public Session updateListener(Update descriptionUpdate) {
						if(descriptionUpdate.hasMessage() && descriptionUpdate.getMessage().hasText()) {
							String text = descriptionUpdate.getMessage().getText();
							
							if(text.length() > 256) {
								bot.sendText(userId, "опис дедлайну не повинен перевищувати 256 символів");
							} else {
								deadline.setDescription(text);
								bot.sendText(userId, "__опис дедлайну змінено__");
								sendEditText();
								return editDeadlineSession;
							}
						}
						return this;
					}
				};
			} else if (query.equals("messages")) {
				
				EditMessageText editMessage = new EditMessageText()
						.setChatId(userId)
						.setMessageId(messageId)
						.setText("__напиши нові додаткові повідомлення__\nдля завершення напиши /build")
						.setParseMode("markdown");
				
				bot.execute(editMessage);
				
				return new Session(bot, userId) {
					
					private Set<Long> messageIds = new HashSet<>();
					
					public Session updateListener(Update messagesUpdate) {
						if(messagesUpdate.hasMessage()) {
							if("/build".equalsIgnoreCase(messagesUpdate.getMessage().getText())) {
								bot.sendText(userId, "__додаткові повідомлення змінено__");
								deadline.setMessageIds(messageIds);
								deadline.setChatId(userId);
								sendEditText();
								return editDeadlineSession;
								
							} else {
								messageIds.add((long) (int) update.getMessage().getMessageId());
							}							
						}
						
						return this;
					}
				};
				
			} else if (query.equals("save")) {
				return new ConfirmEditDeadlineSession(bot, userId, deadline);
			}
		}
		
		
		return this;
		
	}
	
}