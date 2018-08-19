package telegram.bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.User;
import model.dao.CommunityDao;
import model.dao.CommunityDaoMySql;
import model.dao.DeadlineDao;
import model.dao.DeadlineDaoMySql;
import model.dao.UserDao;
import model.dao.UserDaoMySql;
import telegram.session.CreateCommunitySession;
import telegram.session.CreateDeadlineSession;
import telegram.session.MyAdminSession;
import telegram.session.MyCommunitiesSession;
import telegram.session.MyDeadlinesSession;
import telegram.session.SearchCommunitySession;
import telegram.session.api.SessionContainer;

public class KMADeadlineBot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() {
		return "KMADeadlineBot";
	}

	@Override
	public String getBotToken() {
		return "546487698:AAH-BB8KRJoEsNbPNZQWMWAXlqn4E4dFI64"/* "546487698:AAH-BB8KRJoEsNbPNZQWMWAXlqn4E4dFI64" */;
	}
	// https://api.telegram.org/bot546487698:AAH-BB8KRJoEsNbPNZQWMWAXlqn4E4dFI64/editMessageText?chat_id=425956289&message_id=799&text=chang**ed**text
	// dao

	public final UserDao userDao = new UserDaoMySql();
	public final CommunityDao communityDao = new CommunityDaoMySql();
	public final DeadlineDao deadlineDao = new DeadlineDaoMySql();

	// sessions

	public final SessionContainer sessionContainer = new SessionContainer();

	// update listener

	@Override
	public void onUpdateReceived(Update update) {
		long userId = getUserId(update);
		// check main commands
		if (update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase();

			if (text.equals("/create_community")) {
				sessionContainer.add(new CreateCommunitySession(this, userId));
				return;
			} else if (text.equals("/search_community")) {
				sessionContainer.add(new SearchCommunitySession(this, userId));
				return;
			} else if (text.equals("/my_communities")) {
				sessionContainer.add(new MyCommunitiesSession(this, userId));
				return;
			} else if (text.equals("/my_admin_communities")) {
				sessionContainer.add(new MyAdminSession(this, userId));
				return;
			} else if (text.equals("/my_deadlines")) {
				sessionContainer.add(new MyDeadlinesSession(this, userId));
				return;
			} else if (text.equals("/create_deadline")) {
				sessionContainer.add(new CreateDeadlineSession(this, userId));
				return;
			} else if (text.equals("/home")) {
				sendMenuMessage(userId);
				return;
			}
		}

		// check session container
		if (sessionContainer.contains(userId)) {
			sessionContainer.get(userId).process(update);
		}

		// register new user
		if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/start")) {
			
			if (!userDao.contains(userId)) {
				userDao.insert(new User(userId));	
			}
			
			sendMenuMessage(userId);
		}
	}

	public void sendStartMessage(long userId) {
		String text = "--- this is @KMADeadlineBot ---";
		sendText(userId, text);
	}
	
	public void sendMenuMessage(long userId) {
		sessionContainer.remove(userId); // ! ! !
		String text = "@KMADeadlineBot\n\n"
				+ "/create_deadline - <i>створити дедлайн</i>\n"
				+ "/my_deadlines - <i>мої дедлайни</i>\n"
				+ "/my_communities - <i>мої спільноти</i>"
				+ "/my_admin_communities - <i>спільноти де я є адміном</i>"
				+ "/search_community - <i>шукати спільноту</i>"
				+ "/create_community - <i>створити спільноту</i>"
				+ "/home - <i>показати головне меню</i>";
		sendText(userId, text);
	}

	// send simple text message with parse mode
	public void sendText(long receiverId, String message) {

		SendMessage sendMessage = new SendMessage().setChatId(receiverId).setText(message).setParseMode("markdown");
		sendMessage.setParseMode("HTML");
		try {
			execute(sendMessage);
		} catch (TelegramApiException exception) {
			exception.printStackTrace();
		}
	}

	// static methods

	public static long getUserId(Update update) {
		if (update.hasMessage()) {
			// if this update contains message, get id from message
			return update.getMessage().getFrom().getId();
		} else {
			// else get id from callback quary
			return update.getCallbackQuery().getFrom().getId();
		}
	}

	public static void main(String[] args) throws InterruptedException, TelegramApiException {
		ApiContextInitializer.init();
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		telegramBotsApi.registerBot(new KMADeadlineBot());
	}
}
