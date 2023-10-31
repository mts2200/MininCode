package ru.mts2200.minincode.bot.command.impl.commands.admin;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.sql.manager.TableManager;
import ru.mts2200.minincode.bot.sql.model.Group;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.bot.util.FormatHelper;
import ru.mts2200.minincode.launch.config.GlobalConfig;

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "CreateGroupCommand")
public final class CreateGroupCommand extends Command {
	private static final String SUCCESS = "Группа %s успешно создана!";
	private static final String WRONG_FORMAT = "Неверная аббревиатура группы!";
	private static final String NEED_NAME = "Необходимо ввести аббревиатуру названия группы!";

	public CreateGroupCommand() {
		super("new_group", "Создать новую группу (Admin)");
	}

	@Override
	public boolean canUseCommand(User user) {
		return GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var message = SendMessage.builder().chatId(chat.getId());

			if (!arguments.isEmpty()) {
				var groupName = arguments.get(0);

				if (!Strings.isNullOrEmpty(groupName) && FormatHelper.IT_CUBE_GROUP_PATTERN.matcher(groupName).matches()) {
					TableManager.insertGroup(Group.builder().groupName(groupName).build());
					message.text(SUCCESS.formatted(groupName)).parseMode(ParseMode.MARKDOWN);
				} else {
					message.text(WRONG_FORMAT);
				}
			} else {
				message.text(NEED_NAME);
			}

			try {
				bot.executeAsync(message.build());
			} catch (TelegramApiException e) {
				log.error(Errors.TELEGRAM_API, e);
				throw new RuntimeException(e);
			}
		});
	}
}
