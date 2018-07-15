package telegram.bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import model.User;
import model.dao.CommunityDao;
import model.dao.CommunityDaoMySql;
import model.dao.DeadlineDao;
import model.dao.DeadlineDaoMySql;
import model.dao.UserDao;
import model.dao.UserDaoMySql;
import telegram.session.ConfirmEditMemberListSession;
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
	public String getBotToken() { return "546487698:AAH-BB8KRJoEsNbPNZQWMWAXlqn4E4dFI64"; }
	//https://api.telegram.org/bot546487698:AAH-BB8KRJoEsNbPNZQWMWAXlqn4E4dFI64/editMessageText?chat_id=425956289&message_id=799&text=chang**ed**text
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
	
		
		if(sessionContainer.contains(userId)) {
			sessionContainer.get(userId).process(update);
		} else if(update.hasMessage() && update.getMessage().hasText()) {
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
			} else if (text.equals("/reg")) {
				Community c = communityDao.select("asd");
				sessionContainer.add(new ConfirmEditMemberListSession(this, userId, c));
			}
		} 
		
		if (update.hasMessage() && update.getMessage().hasText()
				&& update.getMessage().getText().equals("/start") && !userDao.contains(userId)) {			
			sendStartMessage(userId);
			sessionContainer.add(new MenuSession(this, userId));
			userDao.insert(new User(userId));
		}
	}
	
	public void sendStartMessage(long userId) {
		String text = "--- this is @KMADeadlineBot ---";
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
