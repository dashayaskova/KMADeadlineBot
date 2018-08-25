package telegram.session;

import java.util.Iterator;
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

public class MyCommunitiesSessionDasha extends Session {

	private Set<String> communityNames;
	private int page = 0;
	private int numOfMemInSes = 10;
	private String chatId;
	private Integer messageId;

	
	private int getNumOfPages() {
		return (int) Math.ceil(communityNames.size() / (float) numOfMemInSes);
	}

	public  MyCommunitiesSessionDasha(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("->", "0").nextRow()
				.build();
		
		this.chatId = sm.getChatId();
		sm.setText(sendListOfCommunities(page));

		try {
			Message mes = bot.execute(sm);
			this.messageId = mes.getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		
		
	}

	public String sendListOfCommunities(int num) {
		communityNames = bot.communityDao.selectNamesByMemberId(userId);
	
		StringBuffer text = new StringBuffer();
		text.append("The list of communities, where you are an member:\n");
		text.append("Page"+"(" + (num + 1) + "/" + getNumOfPages() + "):\n");

		Iterator<String> iterator = communityNames.iterator();

		for (int i = 0; i < num * numOfMemInSes; i++) {
			iterator.next();
		}

		int count = 0;
		while (count < numOfMemInSes && iterator.hasNext()) {
			String name = iterator.next();
			text.append("/_" + name + "\n");
			count++;
		}

		text.append("\nYou can select community, by clicking on it. To return click: /my_communities or /home");
		
		return text.toString();
	}
	
	
	private void edit(Update update,int num) {
		EditMessageText editMessage = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("->", "0").nextRow()
				.buildEdit();
		editMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
		editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
		editMessage.setText(sendListOfCommunities(num));
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
			if (update.getMessage().getText().startsWith("/_")) {
				String text = update.getMessage().getText();
				if (communityNames.contains(text.substring(2))) {
					  EditMessageText editMessage = new EditMessageText();
			          editMessage.setChatId(String.valueOf(chatId));
			          editMessage.setText(sendListOfCommunities(page));
			          editMessage.setMessageId(messageId);
			          bot.execute(editMessage);
					return new CommunityOptionsSession(bot, userId, update.getMessage().getText().substring(2));

				}
			}
		}

		bot.sendText(userId, "choose community name which is in the list");
		return this;
	}
}
