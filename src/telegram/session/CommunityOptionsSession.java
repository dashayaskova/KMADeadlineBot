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

/** @author mike_mars */

public class CommunityOptionsSession extends Session {

	private Community community;
	private Integer lastSent;

	/**
	 * @param bot
	 * @param userId
	 * @param communityName
	 */
	public CommunityOptionsSession(KMADeadlineBot bot, long userId, String communityName) {
		super(bot, userId);
		this.community = bot.communityDao.select(communityName);
		init();
	}

	public CommunityOptionsSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
		init();
	}
	
	private void init() {
		String text = "**" + community.getName() + "**\n" + "Choose one of the following:";

		InlineKeyboardBuilder kb = InlineKeyboardBuilder.create(userId).setText(text);
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
		SendMessage sm = kb.build();
		try {
			lastSent = ((Message) bot.execute(sm)).getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			StringBuffer text = new StringBuffer();
			EditMessageText et = new EditMessageText();
			et.setChatId(userId);
			et.setMessageId(lastSent);
			et.setParseMode("HTML");

			int t = new Integer(update.getCallbackQuery().getData());
			switch (t) {
			case 11:
				text.append("_creating deadline_");
				break;
			case 12:
				text.append("_showing deadlines_");
				break;
			case 13:
				text.append("_Editing member list_");
				break;
			case 14:
				text.append("_Editing adim list_");
				break;
			case 15:
				text.append("_You are no longer a member of the community_");
				break;
			case 16:
				text.append("_deleteing group_");
				break;
			case 21:
				text.append("_Show deadlines_");
				break;
			case 22:
				text.append("<i>Member list:</i>");
				Set<Long> member_ids = community.getMemberIds();
				
				if(member_ids.isEmpty()) {
					text.append("\nThis list is empty");
					break;
				}
				for(long id : member_ids) {
					text.append("\n@"+bot.execute(new GetChat(id)).getUserName());
				}
				break;
			case 23:
				text.append("<i>Admin list:</i>");
				Set<Long> admin_ids = community.getAdminIds();
				if(admin_ids.isEmpty()) {
					text.append("\nThis list is empty");
					break;
				}
				for(long id : admin_ids) {
					text.append("\n@"+bot.execute(new GetChat(id)).getUserName());
				}
				break;
			case 24:
				text.append("_You are the member of community now_");
				community.addMemberId(userId);
				bot.communityDao.update(community);
			}
			
			et.setText(text.toString());
			bot.execute(et);
			bot.sessionContainer.remove(userId);
			
			switch (t) {
			case 11:
				// TODO bot.sessionContainer.add(new CreateDeadlineSession(bot, userId));
				break;
			case 12:
				// TODO
				break;
			case 13:
				return new EditMemberListSessionMisha(bot, userId, community.getName());
			case 14:
				// TODO
				break;
			case 15:
				return new MenuSession(bot, userId);
			case 16:
				// TODO
			case 21:
				// TODO
				break;
			case 22:
				return new CommunityOptionsSession(bot, userId, community);
			case 23:
				return new CommunityOptionsSession(bot, userId, community);
			case 24:
				return new CommunityOptionsSession(bot, userId, community);
}
		}
		return null;
	}

}
