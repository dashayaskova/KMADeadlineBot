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

		info.append(
				"\n\nContinue searching: /search_community\nReturn home: /home\nTo manage this community choose one of the following:");
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

	public CommunityOptionsSession(KMADeadlineBot bot, long userId, Community community, int messageId, StringBuffer info) {
		super(bot, userId);
		this.info = info;
		this.community = community;
		EditMessageText et = new EditMessageText();
		et.setChatId(userId);
		et.setMessageId(messageId);
		et.setText(info.toString());
		et.setParseMode("HTML");
		et.setReplyMarkup(getKeyboard());
		try {
			bot.execute(et);
		} catch (TelegramApiException e) {e.printStackTrace();}
	}

	private InlineKeyboardMarkup getKeyboard() {
		InlineKeyboardBuilder kb = InlineKeyboardBuilder.create(userId);
		if (community.isAdmin(userId)) {
			kb.addButton("1. Create deadline", "11").addButton("2. View deadlines", "12").nextRow()
					.addButton("3. Show member list", "22").addButton("4. Show admin list", "23").nextRow()
					.addButton("5. Edit member list", "13").addButton("6. Edit admin list", "14").nextRow()
					.addButton("7. Leave group", "15").addButton("8. Delete group", "16").nextRow();
		} else if (community.isMember(userId)) {
			kb.addButton("1. Show me deadline", "21").nextRow().addButton("2. Show member list", "22")
					.addButton("3. Show admin list", "23").nextRow().addButton("4. Leave", "15").nextRow();
		} else {
			kb.addButton("1. Show me deadline", "21").nextRow().addButton("2. Show member list", "22")
					.addButton("3. Show admin list", "23").nextRow().addButton("4. Join", "24").nextRow();
		}
		return kb.getReplyMarkup();
	}

	private InlineKeyboardMarkup getYesNoKeyboard() {
		InlineKeyboardBuilder kb = InlineKeyboardBuilder.create(userId);
		kb.addButton("Yes", "1").addButton("No", "0").nextRow();
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
				text.append("<b>--->Editing adim list</b>\nIf you want to remove someone from admin list click <i>remove admins</i>, "
						+ "if you want to make another person an admin click <i>promote admins</i>");
				InlineKeyboardMarkup markup = InlineKeyboardBuilder.create(userId).addButton("Remove admins", "r")
						.addButton("Promote admins", "p").nextRow().addButton("Back", "b").nextRow().getReplyMarkup(); 
				et.setReplyMarkup(markup);
				break;
			case 15:
				text.append("<b>--->You are no longer a member of the community</b>");
				break;
			case 16:
				text.append("<b>--->Are you sure you want to delete this community?</b>");
				et.setReplyMarkup(getYesNoKeyboard());
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
				et.setReplyMarkup(getKeyboard());
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
				et.setReplyMarkup(getKeyboard());
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
				return new CreateDeadlineSession(bot, userId, community.getName());
			case 12:
				return new CommunityDeadlinesSession(bot, userId, community);
			case 13:
				return new EditMemberListSessionMisha(bot, userId, community);
			case 14:
				return new Session(bot, userId) {
					@Override
					public Session updateListener(Update update) throws TelegramApiException {
						if(update.hasCallbackQuery()) {
							et.setReplyMarkup(null);
							if(update.getCallbackQuery().getData().equals("r")) {
								et.setText(info.toString() + "\n\n<b>-->Removing admins</b>");
								bot.execute(et);
								return new EditAdminListSession(bot, userId, community);
							}
							if(update.getCallbackQuery().getData().equals("p")) {
								et.setText(info.toString() + "\n\n<b>-->Promoting admins</b>");
								bot.execute(et);
								return new PromoteSession(bot, userId, community);
							}
							if(update.getCallbackQuery().getData().equals("b")) 
								return new CommunityOptionsSession(bot, userId, community, update.getCallbackQuery().getMessage().getMessageId(), info);
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
				return new Session(bot, userId) {
					@Override
					public Session updateListener(Update update) throws TelegramApiException {
						if(update.hasCallbackQuery()) {
							if(update.getCallbackQuery().getData().equals("1")) {
								bot.communityDao.delete(community.getName());
								EditMessageText et = new EditMessageText();
								et.setChatId(userId);
								et.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
								et.setText("This community no longer exists!!!");
								bot.execute(et);
								bot.sendMenuMessage(userId);
								bot.sessionContainer.remove(userId);
								return null;
							} else {
								return new CommunityOptionsSession(bot, userId, community, update.getCallbackQuery().getMessage().getMessageId(), info);
							}
						}
						return null;
					}
				};
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
