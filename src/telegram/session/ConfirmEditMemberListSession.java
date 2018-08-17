package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author mike_mars */

public class ConfirmEditMemberListSession  extends Session {

	private Community community;
	private long userId;
	private Set<Long> deleted;

	public ConfirmEditMemberListSession(KMADeadlineBot bot, long userId, Community community, Set<Long> deleted) {
		super(bot, userId);
		this.community = community;
		this.deleted = deleted;
		
		StringBuffer text = new StringBuffer();
		text.append("Are u sure that u want to delete these people?");
		for(long id : deleted) {
			try {
				String name = bot.execute(new GetChat(id)).getUserName();
				text.append("\n@"+name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("Yes", "1").addButton("No", "0").nextRow()
				.build();
		sm.setText(text.toString());
		try {
			bot.execute(sm);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}	
		}
	
	public ConfirmEditMemberListSession(KMADeadlineBot bot, long userId, String communityName, Set<Long> deleted) {
		this(bot, userId, bot.communityDao.select(communityName), deleted);
	}
	
	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasCallbackQuery()) {
		String text = update.getCallbackQuery().getData();
		
		EditMessageText editMessage = new EditMessageText(); 
		editMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
		editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId()); 
				
		if(text.equals("1")) {
			deleted.forEach(l->community.removeMemberId(l));
			bot.communityDao.update(community);
			editMessage.setText("You removed "+deleted.size()+" members from the community "+community.getName());
			bot.execute(editMessage);
		    return new CommunityOptionsSession(bot, userId, community);
		}else if(text.equals("0")) {
			editMessage.setText("You didn't remove anyone from the community "+community.getName());
			bot.execute(editMessage);
			return new CommunityOptionsSession(bot, userId, community);
		}}
		return this;
	}

}
