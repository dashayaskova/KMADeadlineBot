package session;

import org.telegram.telegrambots.api.objects.Update;

public interface UpdateListener {
	
	void execute(Update update);

}

