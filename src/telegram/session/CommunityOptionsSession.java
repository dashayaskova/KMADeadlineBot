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
		String text = "**" + communityName + "**\n" + "Для продовження виберіть наступне:";

		InlineKeyboardBuilder kb = InlineKeyboardBuilder.create(userId).setText(text);
		if (community.isAdmin(userId)) {
			kb.addButton("1. Створити дедлайн", "11").addButton("2. Переглянути дедлайни", "12").nextRow()
					.addButton("3. Редагувати список учасників", "13").addButton("4. Регувати список адмінів", "14")
					.nextRow().addButton("5. Залишити групу", "15").addButton("6. Видалити  групу", "16").nextRow();
		} else {
			kb.addButton("1. Show me deadline", "21").nextRow()
			.addButton("2. Show member list", "22").addButton("3. Show admin list", "23")
			.nextRow().addButton("4. Leave", "15").nextRow();
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
			String text = "";
			EditMessageText et = new EditMessageText();
			et.setChatId(userId);
			et.setMessageId(lastSent);
			et.setParseMode("Markdown");

			int t = new Integer(update.getCallbackQuery().getData());
			switch (t) {
			case 11:
				text = "_Створення дедлайну_";
				break;
			case 12:
				text = "_Поточні дедлайни_";
				break;
			case 13:
				text = "_Редагування учасників_";
				break;
			case 14:
				text = "_Редагування адміністраторів_";
				break;
			case 15:
				text = "_Ви впевнені, що хочете залишити групу?_";
				break;
			case 16:
				text = "_Ви впевнені, що хочете видалити групу?_";
				break;
			case 21:
				text = "_Show deadlines_";
				break;
			case 22:
				text = "_Member list_";
				break;
			case 23:
				text = "_Admin list_";
			}
			et.setText(text);
			System.out.println(bot.execute(et));
			bot.sessionContainer.remove(userId);
			switch (t) {
			case 11:
				// TODO bot.sessionContainer.add(new CreateDeadlineSession(bot, userId));
				break;
			case 12:
				// TODO
				break;
			case 13:
				// TODO
				break;
			case 14:
				// TODO
				break;
			case 15:
				// TODO
				break;
			case 16:
				// TODO
			case 21:
				// TODO
				break;
			case 22:
				// TODO
				break;
			case 23:
				// TODO
				break;
}
		}
		return null;
	}

}
