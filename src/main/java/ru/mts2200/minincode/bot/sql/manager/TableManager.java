package ru.mts2200.minincode.bot.sql.manager;

import com.google.common.collect.ImmutableSet;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.bot.sql.model.Award;
import ru.mts2200.minincode.bot.sql.model.Document;
import ru.mts2200.minincode.bot.sql.model.Group;
import ru.mts2200.minincode.bot.sql.model.Student;
import ru.mts2200.minincode.bot.util.Compressor;
import ru.mts2200.minincode.bot.util.Errors;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author mts2200
 */
@UtilityClass
@Slf4j(topic = "SQL")
@SuppressWarnings("unused")
public final class TableManager {
	// TODO: Use Hibernate

	// SQL запросы
	private static final String CREATE_GROUPS_TABLE = "CREATE TABLE IF NOT EXISTS `groups`( `group_id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `group_name` VARCHAR(10) NOT NULL UNIQUE );";
	private static final String CREATE_DOCUMENTS_TABLE = "CREATE TABLE IF NOT EXISTS `documents`( `document_id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `document_name` VARCHAR(255), `document_binary` MEDIUMBLOB );";
	private static final String CREATE_STUDENTS_TABLE = "CREATE TABLE IF NOT EXISTS `students`( `student_id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `first_name` VARCHAR(20), `last_name` VARCHAR(20), `surname` VARCHAR(20), `age` INT, `group_id` INT, `access_token` VARCHAR(36) NOT NULL UNIQUE DEFAULT UUID(), FOREIGN KEY (`group_id`) REFERENCES `groups`(`group_id`) ON DELETE CASCADE );";
	private static final String CREATE_AWARDS_TABLE = "CREATE TABLE IF NOT EXISTS `awards`( `award_id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `student_id` INT, `document_id` INT, FOREIGN KEY (`student_id`) REFERENCES `students`(`student_id`) ON DELETE CASCADE, FOREIGN KEY (`document_id`) REFERENCES `documents`(`document_id`) ON DELETE CASCADE );";

	private static final String INSERT_GROUP = "INSERT INTO `groups`(`group_name`) VALUES(?);";
	private static final String INSERT_DOCUMENT = "INSERT INTO `documents`(`document_name`, `document_binary`) VALUES(?, ?);";
	private static final String INSERT_STUDENT = "INSERT INTO `students`(`first_name`, `last_name`, `surname`, `age`, `group_id`, `access_token`) VALUES(?, ?, ?, ?, ?, ?);";
	private static final String INSERT_AWARD = "INSERT INTO `awards`(`student_id`, `document_id`) VALUES(?, ?);";

	private static final String SELECT_GROUP_BY_ID = "SELECT * FROM `groups` WHERE `group_id` = ? ORDER BY `group_id` ASC;";
	private static final String SELECT_DOCUMENT_BY_ID = "SELECT * FROM `documents` WHERE `document_id` = ? ORDER BY `document_id` ASC;";
	private static final String SELECT_STUDENT_BY_ID = "SELECT * FROM `students`, `groups` WHERE `students`.`group_id` = `groups`.`group_id` AND `students`.`student_id` = ? ORDER BY `students`.`student_id` ASC;";
	private static final String SELECT_AWARD_BY_ID = "SELECT * FROM `awards`, `students`, `documents` WHERE `awards`.`student_id` = `students`.`student_id` AND `awards`.`document_id` = `documents`.`document_id` AND `awards`.`award_id` = ? ORDER BY `award_id` ASC";

	private static final String SELECT_ALL_GROUPS = "SELECT * FROM `groups` ORDER BY `group_id` ASC;";
	private static final String SELECT_ALL_DOCUMENTS = "SELECT * FROM `documents` ORDER BY `document_id` ASC";
	private static final String SELECT_ALL_STUDENTS = "SELECT * FROM `students`, `groups` WHERE `students`.`group_id` = `groups`.`group_id` ORDER BY `students`.`student_id` ASC;";
	private static final String SELECT_ALL_AWARDS = "SELECT * FROM `awards`, `students`, `documents` WHERE `awards`.`student_id` = `students`.`student_id` AND `awards`.`document_id` = `documents`.`document_id` ORDER BY `award_id` ASC;";

	private static final String SELECT_GROUP_BY_NAME = "SELECT * FROM `groups` WHERE `group_name` = ?;";
	private static final String SELECT_STUDENT_BY_ACCESS_TOKEN = "SELECT * FROM `students`, `groups` WHERE `access_token` = ? AND `students`.`group_id` = `groups`.`group_id`;";
	private static final String SELECT_STUDENTS_BY_LAST_NAME = "SELECT * FROM `students`, `groups` WHERE LOWER(`students`.`last_name`) = LOWER(?) AND `students`.`group_id` = `groups`.`group_id` ORDER BY `students`.`student_id` ASC;";
	private static final String SELECT_AWARDS_BY_STUDENT_ID = "SELECT * FROM `awards`, `students`, `groups`, `documents` WHERE `awards`.`student_id` = ? AND `students`.`group_id` = `groups`.`group_id` AND `awards`.`document_id` = `documents`.`document_id` ORDER BY `award_id` ASC";

	// Вспомогательные методы

	/**
	 * @return Поток базы данных {@link DatabaseManager#MAIN_DB}
	 */
	private HikariDataSource mainPool() {
		return DatabaseManager.INSTANCE.connection(DatabaseManager.MAIN_DB);
	}

	/**
	 * Создаёт все необходимые таблицы.
	 */
	public static void createTables() {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.createStatement()) {
				statement.addBatch(CREATE_GROUPS_TABLE);
				statement.addBatch(CREATE_DOCUMENTS_TABLE);
				statement.addBatch(CREATE_STUDENTS_TABLE);
				statement.addBatch(CREATE_AWARDS_TABLE);
				statement.executeBatch();
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}
	}

	// Группы

	/**
	 * Добавляет новую группу в таблицу `groups`.
	 *
	 * @param group группа
	 */
	public static void insertGroup(Group group) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(INSERT_GROUP)) {
				statement.setString(1, group.getGroupName());

				if (statement.executeUpdate() > 0) {
					log.info("Зарегистрирована новая группа с названием {}", group.getGroupName());
				}
			} catch (SQLIntegrityConstraintViolationException ignored) {
				// NO-OP
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}
	}

	/**
	 * @param id Идентификатор группы
	 * @return группа с соответствующим идентификатором
	 */
	public static Optional<Group> selectGroup(int id) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_GROUP_BY_ID)) {
				statement.setInt(1, id);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Group.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	/**
	 * @return Все значения {@link Group} из таблицы `groups`
	 */
	public static Set<Group> selectAllGroups() {
		ImmutableSet.Builder<Group> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_ALL_GROUPS)) {
				var result = statement.executeQuery();

				while (result.next()) {
					builder.add(Group.readFromSQL(result).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	/**
	 * @param name название группы
	 * @return группа с соответствующим названием
	 */
	public static Optional<Group> selectGroupByName(String name) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_GROUP_BY_NAME)) {
				statement.setString(1, name);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Group.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	// Студенты

	/**
	 * Добавляет нового студента в таблицу `students`. Данный метод игнорирует student_id и accessToken.
	 *
	 * @param student студент
	 * @return access_token этого студента
	 */
	public static String insertStudent(Student student) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(INSERT_STUDENT)) {
				var token = UUID.randomUUID().toString();
				statement.setString(1, student.getFirstName());
				statement.setString(2, student.getLastName());
				statement.setString(3, student.getSurname());
				statement.setInt(4, student.getAge());
				statement.setInt(5, student.getGroup().getGroupId());
				statement.setString(6, token);

				if (statement.executeUpdate() > 0) {
					log.info("Зарегистрирован новый студент. {}", student);
					return token;
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return "";
	}

	/**
	 * @param id идентификатор студента
	 * @return студент с соответствующим идентификатором
	 */
	public static Optional<Student> selectStudent(int id) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_STUDENT_BY_ID)) {
				statement.setInt(1, id);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Student.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	/**
	 * @param token приватный токен студента
	 * @return студент с соответствующим токеном
	 */
	public static Optional<Student> selectStudentByToken(String token) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_STUDENT_BY_ACCESS_TOKEN)) {
				statement.setString(1, token);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Student.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	/**
	 * @param lastName фамилия
	 * @return список студентов с соответствующей фамилией
	 */
	public static Set<Student> selectStudentsByLastName(String lastName) {
		ImmutableSet.Builder<Student> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_STUDENTS_BY_LAST_NAME)) {
				statement.setString(1, lastName.trim());
				var result = statement.executeQuery();

				while (result.next()) {
					builder.add(Student.readFromSQL(result).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	/**
	 * @return Все значения {@link Student} из таблицы `students`
	 */
	public static Set<Student> selectAllStudents() {
		ImmutableSet.Builder<Student> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_ALL_STUDENTS)) {
				var result = statement.executeQuery();

				while (result.next()) {
					builder.add(Student.readFromSQL(result).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	// Документы

	/**
	 * Добавляет новый документ в таблицу `documents`.
	 *
	 * @param document документ
	 * @return ID документа в таблице.
	 */
	public static int insertDocument(Document document) {
		var compressed = Compressor.compress(document.getDocumentBinary());

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, document.getDocumentName());
				statement.setBytes(2, compressed);

				if (statement.executeUpdate() > 0) {
					var result = statement.getGeneratedKeys();
					if (result.next()) {
						log.info("В базе данных сохранён новый файл: {}", document.getDocumentName());
						return result.getInt(1);
					}
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return -1;
	}

	/**
	 * @param id идентификатор документа
	 * @return документ с соответствующим идентификатором
	 */
	public static Optional<Document> selectDocument(int id) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_DOCUMENT_BY_ID)) {
				statement.setInt(1, id);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Document.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	/**
	 * @return Все значения {@link Document} из таблицы `documents`
	 */
	public static Set<Document> selectAllDocuments() {
		ImmutableSet.Builder<Document> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_ALL_DOCUMENTS)) {
				var result = statement.executeQuery();

				while (result.next()) {
					builder.add(Document.readFromSQL(result).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	// Награды

	/**
	 * Добавляет новую награду в таблицу `awards`.
	 *
	 * @param award награда
	 */
	public static void insertAward(Award award) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(INSERT_AWARD)) {
				statement.setInt(1, award.getStudent().getStudentId());
				statement.setInt(2, award.getDocument().getDocumentId());

				if (statement.executeUpdate() > 0) {
					log.info("Добавлена новая награда для студента с ID: {}. ID документа: {}", award.getStudent().getStudentId(), award.getDocument().getDocumentId());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}
	}

	/**
	 * @param id идентификатор необходимого {@link Award} из таблицы `awards`
	 * @return награда по ключу
	 */
	public static Optional<Award> selectAward(int id) {
		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_AWARD_BY_ID)) {
				statement.setInt(1, id);

				var resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return Optional.of(Award.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return Optional.empty();
	}

	/**
	 * @return Все значения {@link Award} из таблицы `awards`
	 */
	public static Set<Award> selectAllAwards() {
		ImmutableSet.Builder<Award> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_ALL_AWARDS)) {
				var resultSet = statement.executeQuery();

				while (resultSet.next()) {
					builder.add(Award.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	/**
	 * @param student студент
	 * @return список наград студента
	 */
	public static Set<Award> selectAwardsByStudent(Student student) {
		ImmutableSet.Builder<Award> builder = ImmutableSet.builder();

		try (var connection = mainPool().getConnection()) {
			try (var statement = connection.prepareStatement(SELECT_AWARDS_BY_STUDENT_ID)) {
				statement.setInt(1, student.getStudentId());

				var resultSet = statement.executeQuery();
				while (resultSet.next()) {
					builder.add(Award.readFromSQL(resultSet).build());
				}
			} catch (SQLException exception) {
				processStatementException(exception);
			}
		} catch (SQLException exception) {
			processConnectionException(exception);
		}

		return builder.build();
	}

	// Утилиты
	private static void processConnectionException(SQLException exception) {
		log.error(Errors.CONNECTION_CREATION, exception);
		throw new RuntimeException(exception);
	}

	private static void processStatementException(SQLException exception) {
		log.error(Errors.STATEMENT_EXECUTE, exception);
		throw new RuntimeException(exception);
	}
}
