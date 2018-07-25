package telegram.session;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author dSigma */

public class ConfirmEditMemberListSession  extends Session {

	private Community community;
	private long deletedId;
	private long userId;

	public ConfirmEditMemberListSession(KMADeadlineBot bot, long userId,long deletedId,Community community) {
		super(bot, userId);
		this.community = community;
		this.deletedId = deletedId;
		this.userId = userId;

		String text = "Are u sure that u want to delete this person?";
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("Yes", "1").addButton("No", "0").nextRow()
				.build();
		sm.setText(text.toString());
		try {
			bot.execute(sm);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasCallbackQuery()) {
		String text = update.getCallbackQuery().getData();
		
		EditMessageText editMessage = new EditMessageText(); 
		editMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
		editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId()); 
		String name = bot.execute(new GetChat(deletedId)).getUserName();
		
		
		if(text.equals("1")) {
			community.removeMemberId(deletedId);
			bot.communityDao.update(community);
			editMessage.setText("You removed "+name+" from the community "+community.getName());
			bot.execute(editMessage);
			
		    return new CommunityOptionsSession(bot, userId, community.getName());
		}else if(text.equals("0")) {
			editMessage.setText("You didn't remove "+name+" from the community "+community.getName());
			bot.execute(editMessage);
			  return new CommunityOptionsSession(bot, userId, community.getName());
		}}
		return this;
	}

}
