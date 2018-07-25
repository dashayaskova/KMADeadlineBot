package telegram.session;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Deadline;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author mike_mars */

public class ConfirmEditDeadlineSession extends Session {
	Deadline deadline;
	
	public ConfirmEditDeadlineSession(KMADeadlineBot bot, long userId, Deadline deadline) {
		super(bot, userId);
		this.deadline = deadline;
		String text = deadline.toString() + "\nDo you want to apply changes?";
		SendMessage sm = InlineKeyboardBuilder.create(userId).setText(text).addButton("Yes", "1").addButton("No", "0").nextRow().build();
		try {
			bot.execute(sm);
		} catch (TelegramApiException e) {e.printStackTrace();}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			CallbackQuery qc = update.getCallbackQuery();
			if(qc.getData().equals("1")) {
				bot.deadlineDao.update(deadline);
				EditMessageText et = new EditMessageText();
				et.setChatId(userId);
				et.setMessageId(qc.getMessage().getMessageId());
				et.setText(qc.getMessage().getText() + "\nChanges applied");
				bot.execute(et);
				bot.sessionContainer.remove(userId);
			} else if(qc.getData().equals("0")) {
				bot.sessionContainer.remove(userId);
				return new EditDeadlineSession(bot, userId, deadline);
			}
		}
		return null;
	}

}
