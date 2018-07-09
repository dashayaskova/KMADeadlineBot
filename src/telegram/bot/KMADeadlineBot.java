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
import telegram.session.MenuSession;
import telegram.session.MyCommunitiesSession;
import telegram.session.MyDeadlinesSession;
import telegram.session.SearchCommunitySession;
import telegram.session.api.SessionContainer;

public class KMADeadlineBot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() { return "KMADeadlineBot"; }

	@Override
	public String getBotToken() { return "474785816:AAGKFWo8RU2IFqg_uqmEGYPhy_W8QvnRDqo"; }
	
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
		
		if(update.hasMessage() && update.getMessage().hasText()) {
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
			} else if (text.equals("/my_deadlines")) {
				sessionContainer.add(new MyDeadlinesSession(this, userId));
				return;
			} else if (text.equals("/create_deadline")) {
				sessionContainer.add(new CreateDeadlineSession(this, userId));
				return;
			} else if (text.equals("/home")) {
				sessionContainer.add(new MenuSession(this, userId));
				return;
			}
		} 
		
		if(sessionContainer.contains(userId)) {
			
			sessionContainer.get(userId).process(update);
			
		} else if (update.hasMessage() && update.getMessage().hasText()
				&& update.getMessage().getText().equals("/start")) {
			
			sendStartMessage(userId);
			sessionContainer.add(new MenuSession(this, userId));
			
			userDao.insert(new User(userId));
		}
	}
	
	public void sendStartMessage(long userId) {
		String text = "--- привіт, тебе вітає @KMADeadlineBot ---";
		sendText(userId, text);
	}

	public void sendText(long receiverId, String message) {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(receiverId);
		sendMessage.setText(message);

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
		System.out.println(System.currentTimeMillis());
		ApiContextInitializer.init();
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		telegramBotsApi.registerBot(new KMADeadlineBot());
	}
}
