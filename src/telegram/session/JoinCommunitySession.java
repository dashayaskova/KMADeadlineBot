package telegram.session;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class JoinCommunitySession extends Session {

	Community community;
	int lastMsg;

	public JoinCommunitySession(KMADeadlineBot bot, long userId, String communityName) {
		super(bot, userId);
		community = bot.communityDao.select(communityName);

		sendGreeting();
	}

	public JoinCommunitySession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;

		sendGreeting();
	}

	private void sendGreeting() {
		if (community.isMember(userId)) {
			bot.sessionContainer.remove(userId);
			bot.sessionContainer.add(new CommunityOptionsSession(bot, userId, community.getName()));
			return;
		}

		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("Join", "join")
				.addButton("Back to search", "leave").nextRow().build();
		sm.setText(community.getName() + "\nNumber of members: " + community.getMemberIds().size()
				+ "\nNumber of admins: " + community.getAdminIds().size());
		try {
			lastMsg = ((Message) bot.execute(sm)).getMessageId();
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			if (update.getCallbackQuery().getData().equals("join")) {
				community.addMemberId(userId);
				bot.communityDao.update(community);

				EditMessageText et = new EditMessageText();
				et.setChatId(userId);
				et.setMessageId(lastMsg);
				et.setText(community.getName() + "\nNumber of members: " + community.getMemberIds().size()
						+ "\nNumber of admins: " + community.getAdminIds().size() + "\n*You joined the communuty*");
				et.setParseMode("Markdown");
				bot.execute(et);
				bot.sessionContainer.remove(userId);
			} else if (update.getCallbackQuery().getData().equals("leave")) {
				bot.sessionContainer.remove(userId);
				bot.sessionContainer.add(new SearchCommunitySession(bot, userId));
			}
		}
		return null;
	}

}
