package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class ConfirmEditMemberListSession extends Session {
	Community community;
	int last;
	String list;

	// TODO add check for callbackId
	public ConfirmEditMemberListSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
		StringBuffer text = new StringBuffer();
		text.append("Member list:\n");

		Set<Long> members = community.getMemberIds();
		try {
			for (long member : members) {
				text.append(bot.execute(new GetChat(member)).getUserName() + "\n");
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		list = text.toString();
		text.append("\nAre you sure you want to apply chanes?");
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("Yes", "1").addButton("No", "0").nextRow()
				.build();
		sm.setText(text.toString());
		try {
			last = ((Message) bot.execute(sm)).getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			int t = new Integer(update.getCallbackQuery().getData());
			switch (t) {
			case 0:
				//TODO
				break;
			case 1:
				bot.communityDao.update(community);
				EditMessageText et = new EditMessageText();

				et.setChatId(userId);
				et.setMessageId(last);
				et.setText(list + "\nChanges applied");
				bot.execute(et);

				bot.sessionContainer.remove(userId);
			}
		}
		return null;
	}

}
