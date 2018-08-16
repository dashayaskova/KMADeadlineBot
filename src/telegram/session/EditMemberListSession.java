package telegram.session;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

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
import test.ConfirmEditMemberListSession;

/** @author dSigma */

public class EditMemberListSession extends Session {
	private Community community;
	private Map map;
	private int page=0;
	private int numOfMemInSes = 1;
	private String chatId;
	private Integer messageId;

	public EditMemberListSession(KMADeadlineBot bot, long userId, String communityName) {
		super(bot, userId);
		this.community = bot.communityDao.select(communityName);
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("->", "0").nextRow()
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
		String text = "������ ��������:" + "(������� " + (num + 1) + "/" + getNumOfPages() + ")";
		Iterator<Long> iterator = community.getMemberIds().iterator();

		for (int i = 0; i < num * numOfMemInSes; i++) {
			iterator.next();
		}

		int count = 0;
		while (count < numOfMemInSes && iterator.hasNext()) {
			try {
				long ids = iterator.next();

				text += "\n" + bot.execute(new GetChat(ids)).getUserName();
				map.put(bot.execute(new GetChat(ids)).getUserName(), ids);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			count++;
		}

		return text += "\n" + "������ ��'� ��������, ����� �� ������ ��������";
	}

	private void edit(Update update,int num) {
		EditMessageText editMessage = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("->", "0").nextRow()
				.buildEdit();
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
	        edit(update,++page);
	        }
	        return this;

	      } else if (update.getCallbackQuery().getData().equals("1")) {
	        if (page > 0) {
	          edit(update,--page);
	        }
	        return this;
	      }
	    }

	    if (update.hasMessage()) {
	      String text = update.getMessage().getText();
	      

	      if (map.containsKey(text)) {
	        long id = (long) map.get(text);
	        if (community.isMember(id)) {
	          EditMessageText editMessage = new EditMessageText();
	          editMessage.setChatId(String.valueOf(chatId));
	          editMessage.setMessageId(messageId);
	          editMessage.setText(sendListOfMembers(page));
	          bot.execute(editMessage);
	          return new ConfirmEditMemberListSession(bot, bot.getUserId(update), id, community);
	        }
	      }
	      SendMessage sm = new SendMessage();
	      sm.setText("We can't find this member in out list");
	      sm.setChatId(chatId);
	      bot.execute(sm);
	    }
	  

	    return this;

	  }
}
