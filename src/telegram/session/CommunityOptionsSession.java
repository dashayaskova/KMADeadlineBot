package telegram.session;

import org.telegram.telegrambots.api.objects.Update;

import model.Community;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class CommunityOptionsSession extends Session {

	private Community community;

	public CommunityOptionsSession(KMADeadlineBot bot, long userId, String communityName) {
		super(bot, userId);
		this.community = bot.communityDao.select(communityName);
		sendCommunityInfo();
	}

	public void sendCommunityInfo() {
		String text = "--- спільнота '" + community.getName() + "' ---\n\n"
			+ "учасники: " + community.getMemberIds().size() + "\n"
			+ "адміністратори: " + community.getAdminIds().size() + "\n\n"
			+ "доступні команди:\n";

		if (community.isMember(userId)) {
			text += "- /leave - вийти зі спільноти\n";
		} else {
			text += "- /join - приєднатись до спільноти\n";
		}
		
		if(community.isMember(userId) || community.isAdmin(userId)) {
			text += "- /deadlines - дедлайни спільноти\n";
		}

		if (community.isAdmin(userId)) {
			text += "для адманістраторів:\n";
			text += "- /admins - адміністратори\n";
			text += "- /members - учасники\n";
		}
		
		text += "\n/home - додому";
		
		bot.sendText(userId, text);
	}

	@Override
	public Session updateListener(Update update) {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase();
			
			if(text.equals("/deadlines")) {
				return null; // new CommunityDeadlinesSession(bot, userId, community);
				
			} else if(text.equals("/join") && !community.isMember(userId)) {
				
				community.addMemberId(userId);
				bot.communityDao.update(community);
				
				bot.sendText(userId, "you joined this community");
				
			} else if(text.equals("/leave") && community.isMember(userId)) {
				
				community.removeMemberId(userId);
				bot.communityDao.update(community);
				
				bot.sendText(userId, "you left this community");
				return new MenuSession(bot, userId);
				
			} else if(text.equals("/admins") && community.isAdmin(userId)) {

				return null; // new MennageAdminsSession(bot, userId, community);
			} else if (text.equals("/members") && community.isAdmin(userId)) {
				
				return null; // new MennageMembersSession(bot, userId, community);
			}
		}
		
		sendCommunityInfo();
		return this;
	}
}
