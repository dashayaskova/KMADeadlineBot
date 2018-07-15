package telegram.session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import model.Deadline;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

public class CreateDeadlineSession extends Session {

	public CreateDeadlineSession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		//askCommunityName();
		bot.sendText(userId, "lol, it works");
		
	}

	private boolean isAdmin = true;
	private Set<Long> messageIds = new HashSet<>();

	private Set<String> communityNames;
	private String communityName;
	private String description;
	private Date date;

	private void askCommunityName() {
		communityNames = bot.communityDao.selectNamesByAdminId(userId);

		if (communityNames.size() == 0) {
			
			bot.sendText(userId, "--- створення нового дедлайну ---\n\n"
					+ "список спільнот де ти маєш право створювати дедлайни порожній\n"
					+ "щоб створювати дедлайни попроси щоб тебе призначили адміністратором, "
						+ "або створи свою спільноту (/create_community)\n\n"
					+ "/home - додому");
			isAdmin = false;
			
		} else {

			bot.sendText(userId, "--- створення нового дедлайну ---\n\n"
					+ "1. напиши назву спільноти для якої буде створено дедлайн\n"
					+ communityNames.stream().collect(Collectors.joining("- ", "\n", "\n\n"))
					+ "/home - додому");
		}
	}
	
	private void askDescription() {
		bot.sendText(userId, "--- створення нового дедлайну ---\n\n"
				+ "2. напиши короткий опис дедлайну\n\n" + "/home - додому");
	}
	
	private void askDate() {
		bot.sendText(userId, "--- cтворення нового дедлайну ---\n\n"
				+ "3. надішли дату дедлайну у форматі 'ДД-MM-РРРР ГГ:ХХ:СС'\n\n" + "/home - додому");
	}
	
	private void askOtherMessages() {
		bot.sendText(userId, "--- створення новго дедлайну ---\n\n"
				+ "4. надішли додаткові повідомлення з текстом, фотографіями та документими.\n"
				+ "коли завершиш напиши команду /build щоб зберегти дедлайн\n\n" + "/home - додому");
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		System.out.println(update.getMessage().getFrom().getUserName() + " : " + update.getMessage().getText());
		
			// listen other messages
		if (isAdmin && communityName != null && description != null && date != null
				&& !"/build".equalsIgnoreCase(update.getMessage().getText())) {
			messageIds.add((long) (int) update.getMessage().getMessageId());

			// listen community name
		} else if (update.hasMessage() && update.getMessage().hasText()) {

			String text = update.getMessage().getText();

			if (isAdmin && communityName == null) {

				text = text.toLowerCase();
				if (communityNames.contains(text)) {
					communityName = text;
					askDescription();
				} else {
					bot.sendText(userId, "у списку спільнот де ти можеш створювати дедлайни немає '" + text + "`");
					askCommunityName();
				}

				// listen description
			} else if (isAdmin && communityName != null && description == null) {

				description = text;
				askDate();

				// listen date
			} else if (isAdmin && communityName != null && description != null && date == null) {
				System.out.println("listen date");
				try {
					System.out.println("in try");
					// date format
					DateFormat parser = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
					System.out.println("after parser");
					Date inputDate = parser.parse(text);
					System.out.println("after inputDate");

					// if old date
					if (inputDate.getTime() <= System.currentTimeMillis()) {
						System.out.println("if true");
						bot.sendText(userId, "не можливо створити дедлайн з датою, що вже минула");
						
					} else {
						System.out.println("else");
						this.date = inputDate;
						System.out.println("before askOtherMessages()");
						askOtherMessages();
					}
				} catch (Exception e) {
					e.printStackTrace();
					bot.sendText(userId, "дата не відповідає формату 'ДД.MM.РРРР ГГ:ХХ:СС'");
				}
				
			} else if (isAdmin && communityName != null && description != null && date != null
					&& update.getMessage().getText().equalsIgnoreCase("/build")) {

				Deadline deadline = bot.deadlineDao.create(date, description, communityName, userId, messageIds);
				bot.sendText(userId, "дедлайн створено");
				
				Community community = bot.communityDao.select(deadline.getCommunityName());
				for (long memberId : community.getMemberIds()) {
					bot.sendText(memberId, "у спільноті '" + community.getName() + "' створено новий дедлайн:\n"
							+ deadline.getDescription());
				}
				
				return new DeadlineOptionsSession(bot, userId, deadline);
			}
		}
		return this;
	}

}
