# MininCode
Техническое задание второго уровня сложности.

## Сборка

### Требования
1. Для компилирования потребуется JDK 17. Например, [LibericaJDK](https://github.com/bell-sw/Liberica/releases).
2. Не забываем установить [JAVA_HOME](https://stackoverflow.com/questions/25270314/setting-java-home-in-windows).

### Компиляция
1. Открыть командную строку.
2. Выполнить команду `gradlew build`.
3. При успешной компиляции, итогом сборки будет: `BUILD SUCCESSFUL`.
4. Скомпилированная программа лежит в директории `compiled`. 

## Запуск

### Требования
1. JRE / JDK 17+
2. Для запуска приложения без библиотек внутри, необходимо указать [classpath](https://stackoverflow.com/questions/18413014/run-a-jar-file-from-the-command-line-and-specify-classpath). Если вы не умеете это делать, то используйте приложение с пометкой `all`.

### Windows
1. Открыть командную строку в директории с нашим приложением.
2. Выполнить команду `java -jar jar-file-name`, где `jar-file-name` - название приложения.
3. Для удобства создайте скрипт запуска (например, `start.bat`) с содержимым:
```bash
@echo off
title MinInCode
cls
java -Dfile.encoding=UTF-8 -jar jar-file-name.jar
pause
exit
```
4. Теперь вы можете запускать приложение, выполнив `start.bat` в командной строке.

### Linux
1. Откройте терминал в директории с нашим приложением.
2. Выполните команду `java -jar jar-file-name`, где `jar-file-name` - название приложения.
3. Для удобства создайте скрипт запуска (например, `launch.sh`) с содержимым:
```bash
#!/bin/bash
java -Dfile.encoding=UTF-8 -jar jar-file-name.jar
```
4. Выполните команду `chmod +x launch.sh`, чтобы сделать скрипт исполняемым.
5. Теперь вы можете запускать приложение, выполнив `./launch.sh` в терминале.

## Конфигурация
При первом запуске приложения, в корневой папке, где оно располагается, создаётся файл `minincode_settings.json`. Он отвечает за конфигурацию вашего бота:
1. `token` - Уникальный токен вашего бота, получить его можно с помощью [BotFather](https://telegram.me/BotFather)
2. `username` - Имя вашего бота в Telegram, которое вы указывали создании бота.
3. `sql` - Настройки подключения к MySQL через JDBC.
4. `admins` - Список ID пользователей Telegram, которые являются Администрацией и имеют расширенный функционал бота.
5. `maxThreads` - Максимальное количество потоков, используемых для выполнения асинхронных методов.