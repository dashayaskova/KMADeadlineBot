package telegram.session;

import java.util.Set;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author mike_mars */

public class CommunityOptionsSession extends Session {

	private Community community;
	private StringBuffer info;
	/**
	 * @param bot
	 * @param userId
	 * @param communityName
	 */
	public CommunityOptionsSession(KMADeadlineBot bot, long userId, String communityName) {
		this(bot, userId, bot.communityDao.select(communityName));
	}

	public CommunityOptionsSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
		info = new StringBuffer("--- <b>" + community.getName().toUpperCase() + "</b> community ---\n");
		if (community.isAdmin(userId)) {
			info.append("<i>You are an adimn of this community</i>");
		} else if (community.isMember(userId)) {
			info.append("<i>You are a member of this community</i>");
		} else {
			info.append("<i>You are not a member of this community</i>");
		}
				
		info.append("\n\nContinue searching: /search_community\nReturn home: /home\nTo manage this community choose one of the following:");
		SendMessage sm = new SendMessage();
		sm.setChatId(userId);
		sm.setParseMode("HTML");
		sm.setText(info.toString());
		sm.setReplyMarkup(getKeyboard());
		try {
			bot.execute(sm);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}		
	}
	
	private InlineKeyboardMarkup getKeyboard() {
		InlineKeyboardBuilder kb = InlineKeyboardBuilder.create(userId);
		if (community.isAdmin(userId)) {
			kb.addButton("1. Create deadline", "11").addButton("2. View deadlines", "12").nextRow()
					.addButton("3. Edit member list", "13").addButton("4. Edit admin list", "14")
					.nextRow().addButton("5. Leave group", "15").addButton("6. Delete group", "16").nextRow();
		} else if (community.isMember(userId)) {
			kb.addButton("1. Show me deadline", "21").nextRow()
			.addButton("2. Show member list", "22").addButton("3. Show admin list", "23")
			.nextRow().addButton("4. Leave", "15").nextRow();
		} else {
			kb.addButton("1. Show me deadline", "21").nextRow()
			.addButton("2. Show member list", "22").addButton("3. Show admin list", "23")
			.nextRow().addButton("4. Join", "24").nextRow();
		}
		return kb.getReplyMarkup();
	}
	
	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			CallbackQuery query = update.getCallbackQuery();
			StringBuffer text = new StringBuffer();
			
			EditMessageText et = new EditMessageText();
			text.append(info + "\n\n");
			et.setChatId(userId);
			et.setMessageId(query.getMessage().getMessageId());
			et.setParseMode("HTML");
			InlineKeyboardMarkup keyboard = getKeyboard();
			
			int t = new Integer(query.getData());
			switch (t) {
			case 11:
				text.append("<b>--->Creating deadline</b>");
				break;
			case 12:
				text.append("<b>--->Showing deadlines</b>");
				break;
			case 13:
				text.append("<b>--->Editing member list</b>");
				break;
			case 14:
				text.append("<b>--->Editing adim list</b>");
				break;
			case 15:
				text.append("<b>--->You are no longer a member of the community</b>");
				break;
			case 16:
				text.append("<b>--->Deleteing group</b>");
				break;
			case 21:
				text.append("<b>--->Show deadlines</b>");
				break;
			case 22:
				text.append("<b>--->Member list:</b>");
				Set<Long> member_ids = community.getMemberIds();
				
				if(member_ids.isEmpty()) {
					text.append("\nThis list is empty");
				} else {
					for(long id : member_ids) {
						text.append("\n@"+bot.execute(new GetChat(id)).getUserName());
					}
				}
				et.setReplyMarkup(keyboard);
				break;
			case 23:
				text.append("<b>--->Admin list:</b>");
				Set<Long> admin_ids = community.getAdminIds();
				if(admin_ids.isEmpty()) {
					text.append("\nThis list is empty");
				} else {
					for(long id : admin_ids) {
						text.append("\n@"+bot.execute(new GetChat(id)).getUserName());
					}
				}
				et.setReplyMarkup(keyboard);
				break;
			case 24:
				text.append("---><b>You are the member of community now</b>");
				community.addMemberId(userId);
				bot.communityDao.update(community);
				et.setReplyMarkup(getKeyboard());
			}
			
			et.setText(text.toString());
			bot.execute(et);
			
			switch (t) {
			case 11:
				//TODO constructor
				return new CreateDeadlineSession(bot, userId);
			case 12:
				return new CommunityDeadlinesSession(bot, userId, community);
			case 13:
				return new EditMemberListSessionMisha(bot, userId, community);
			case 14:
				bot.sendText(userId, "If you want to remove someone from admin list click /remove_admins ,\nif you want to make another person an admin click /promote_admins");
				return new Session(bot, userId) {
					@Override
					public Session updateListener(Update update) throws TelegramApiException {
						if(update.hasMessage()) {
							if(update.getMessage().getText().equals("/remove_admins")) return new EditAdminListSession(bot, userId, community);
							if(update.getMessage().getText().equals("/promote_admins")) return new PromoteSession(bot, userId, community);
						}
						return new CommunityOptionsSession(bot, userId, community);
					}
				};
			case 15:
				community.removeMemberId(userId);
				bot.communityDao.update(community);
				bot.sendText(userId, "You left successfuly");
				bot.sendMenuMessage(userId);
				return null;
			case 16:
				// TODO
			case 21:
				return new CommunityDeadlinesSession(bot, userId, community);
			case 22:
				return this;
			case 23:
				return this;
			case 24:
				return this;
}
		}
		return this;
	}

}
