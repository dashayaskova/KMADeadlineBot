package telegram.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author mike_mars */

public class EditMemberListSessionMisha extends Session {
	private Community community;
	private Map<String, Long> map;
	private int page = 0;
	private int numOfMemInSes = 10;
	private String chatId;
	private Integer messageId;
	private Set<Long> deleted = new HashSet<Long>();

	public EditMemberListSessionMisha(KMADeadlineBot bot, long userId, String communityName) {
		super(bot, userId);
		this.community = bot.communityDao.select(communityName);
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("Del", "d").addButton("->", "0").nextRow()
				.build();

		sm.setText(sendListOfMembers(page).toString());
		this.chatId = sm.getChatId();

		try {
			Message mes = bot.execute(sm);
			this.messageId = mes.getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private int getNumOfPages() {
		return (int) Math.ceil(community.getMemberIds().size() / (float) numOfMemInSes);
	}

	private String sendListOfMembers(int num) {
		map = new HashMap<String, Long>();
		String text = "If you want to remove somebody, click on its username. To undo deleting click on the username again. "
				+ "When you're done, click 'Del' \n"
				+ "List of members: " + "(page " + (num + 1) + "/" + getNumOfPages() + ")";
		Iterator<Long> iterator = community.getMemberIds().iterator();

		for (int i = 0; i < num * numOfMemInSes; i++) {
			iterator.next();
		}

		int count = 0;
		while (count < numOfMemInSes && iterator.hasNext()) {
			try {
				long ids = iterator.next();
				String username = ids +"";/*bot.execute(new GetChat(ids)).getUserName();*/
				text += "\n/_" + username;
				map.put(username, ids);
			} catch (Exception e) {
				e.printStackTrace();
			}

			count++;
		}

		return text;
	}

	private void edit(Update update, int num) {
		EditMessageText editMessage = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("Del", "d").addButton("->", "0")
				.nextRow().buildEdit();
		editMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
		editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
		editMessage.setText(sendListOfMembers(num));
		editMessage.setInlineMessageId(update.getCallbackQuery().getInlineMessageId());
		try {
			bot.execute(editMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {

			if (update.getCallbackQuery().getData().equals("0")) {
				if (page < getNumOfPages() - 1) {
					edit(update, ++page);
				}
				return this;

			} else if (update.getCallbackQuery().getData().equals("1")) {
				if (page > 0) {
					edit(update, --page);
				}
				return this;
			} else if (update.getCallbackQuery().getData().equals("d")) {
				bot.sessionContainer.remove(userId);
				return new ConfirmEditMemberListSessionMisha(bot, userId, community, deleted);
			}
		}

		if (update.hasMessage() && update.getMessage().getText().startsWith("/_")) {
			String text = update.getMessage().getText().substring(2);

			if (map.containsKey(text)) {
				long id = map.get(text);
				if (community.isMember(id)) {
					if(deleted.contains(id)) {
						deleted.remove(id);
					} else {
						deleted.add(id);
					}
				}
			} else {
				bot.sendText(userId, "We can't find this member in out list");
			}
		}

		return this;

	}
}
