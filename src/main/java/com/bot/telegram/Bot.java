package com.bot.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    //Для методов использующих Update
    private Update updateEvent = new Update();

    //Переменные
    private boolean screaming = false; //Переменная для выбора режима ответа бота: Крик/тихо

    private long idGPT = 5815596965L;

    //Клавиатуры (меню)

    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;
    private InlineKeyboardMarkup startkeyboard;

    //Кнопки
    InlineKeyboardButton menu = InlineKeyboardButton.builder()
            .text("Menu")           // Название кнопки
            .callbackData("menu")   // Данные отпраляемые в update
            .build();
    InlineKeyboardButton next = InlineKeyboardButton.builder()
            .text("Next")           // Название кнопки
            .callbackData("next")   // Данные отпраляемые в update
            .build();
    InlineKeyboardButton back = InlineKeyboardButton.builder()
            .text("Back")           // Название кнопки
            .callbackData("back")   // Данные отпраляемые в update
            .build();
    InlineKeyboardButton url = InlineKeyboardButton.builder()
            .text("Tutorial")
            .url("https://core.telegram.org/bots/api")      //Ссылка на сторонний ресурс
            .build();

    //Переопределнные методы
    @Override
    public String getBotUsername() {
        return "SKRID_ONE_BOT";
    }

    @Override
    public String getBotToken() {
        return "7131988516:AAG07SgymNhTlnKJqPKI7N42fN8NKB8ns2g";
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }


    //Метод вызывается автоматически при отправки боту сообщения.
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);

        updateEvent = update;

        //Клавиатуры
        startkeyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(menu))
                .build();

        keyboardM1 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next))
                .build();
        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back, url))
                .build();

        // Если боту отправили сообщение
        if (update.hasMessage()){
            //Сначала надо обработать все команды бота /commands
            //Получение данных: сообщение, отправитель и его id
            var msg = update.getMessage();
            var user = msg.getFrom();
            var id = user.getId();

            //Создание коммандного меню
            createCommands("/start", "Давай начнем всё сначала", "/space", "Взгляни за горизонт событий", "/menu", "Показать меню", "/gpt", "Написать GPT");

            if (msg.isCommand()) {
                //Предполагается, что мы знаем команды бота
                if (msg.getText().equals("/scream")) {
                    screaming = true;
                } else if (msg.getText().equals("/whisper")) {
                    screaming = false;
                } else if (msg.getText().equals("/menu")) {
                    sendMenu(id, "<b>Menu 1</b>", keyboardM1);
                } else if (msg.getText().equals("/space")){
                    System.out.println("SPACE");
                    sendPhoto(id, "C:\\SKRID.jpg");
                } else if (msg.getText().equals("/start")){
                    System.out.println("Start");
                    startMenu(id);
                } else if (msg.getText().equals("/gpt")) {
                    System.out.println("Отправлено сообщение GPT");
                    sendText(idGPT, "/start");
                }

                return;             //Выход из метода, т.к. повторять команды - не нужно.
            }

            //Логика поведения бота
            if(screaming){
                scream(id, msg);
            } else {
                copyMessage(id, msg.getMessageId());
            }

            //Если получили сообщение от idGPT
            if (msg.getFrom().getId() == idGPT) {
                System.out.println("**************************************");
                System.out.println(msg.getText());
            }

        } else if (update.hasCallbackQuery()) {              //Если нажали на кнопку
            //CallBack запрос
            CallbackQuery callbackQuery = update.getCallbackQuery();

            //Данные CallBack запроса
            Long id = callbackQuery.getMessage().getChatId();
            String queryId = callbackQuery.getId();
            String data = callbackQuery.getData();
            int msgId = callbackQuery.getMessage().getMessageId();

            //Метод для обработки нажатия кнопок
            buttonTap(id, queryId, data, msgId);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
        //System.out.println(updates);
    }


    //Методы

    /**
     * Отправка текстового сообщения сообщения
     * @param who кому
     * @param what что
     */
    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    /**
     * Отправка фотографии пользователю
     * @param who кому
     * @param paths Путь к файлу
     */
    private void sendPhoto(Long who, String paths){

        Path path = Paths.get(paths);

        InputFile file = new InputFile(path.toFile());

        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(who.toString())
                .photo(file).build();

        try {
            execute(sendPhoto);                 //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    /**
     * Копирование сообщения и отправка обратно
     * @param who кому
     * @param msgId ответ на какое сообщение
     */
    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())
                .chatId(who.toString())
                .messageId(msgId)
                .build();

        try {
           execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод показывающий стартовое меню (по команде /start)
     * @param who id пользователя для отправки
     */
    public void startMenu (Long who) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text("Приветствую тебя пользователь! Это самый луший telegram bot про космос! \nНажми кнопку меню, чтобы начать путешествие!")
                .replyMarkup(startkeyboard)
                .build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Крик - вывод сообщения CAPS LOCK
     * @param id кому отправить сообщение
     * @param msg сообщение
     */
    public void scream(Long id, Message msg){
        if(msg.hasText()){
            sendText(id, msg.getText().toUpperCase());
        } else {
            copyMessage(id, msg.getMessageId());
        }
    }

    /**
     * Создание меню
     * @param who кому
     * @param txt название меню в формате HTML <> </>
     * @param kb какая клавиатура (нужно собрать заранее)
     */
    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Отправка сообщения с вариантами ответа
     * @param who кому
     * @param what сообщение
     * @param kb клавиатура с вариантами ответа
     */
    public void sendTextWithKeyboard (Long who, String what, InlineKeyboardMarkup kb ) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what)
                .replyMarkup(kb)
                .build();

        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    /**
     * Метод обработки нажатий кнопок
     * @param id Id пользователя кто нажал на кнопку
     * @param queryId Id callback запроса
     * @param data Содержание callback запроса
     * @param msgId Id сообщения
     */
    private void buttonTap(Long id, String queryId, String data, int msgId){
        EditMessageText newTxt = EditMessageText.builder()
                .chatId(id.toString())
                .messageId(Integer.valueOf(msgId))
                .text("").build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder()
                .chatId(id.toString())
                .messageId(Integer.valueOf(msgId))
                .build();

        if (data.equals("next")){
            newTxt.setText("MENU 2");
            newKb.setReplyMarkup(keyboardM2);
        } else if (data.equals("back")) {
            newTxt.setText("MENU 1");
            newKb.setReplyMarkup(keyboardM1);
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId).build();

        try {
            execute(close);
            execute(newTxt);
            execute(newKb);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод возвращает ID текущего Telegram-чата
     */
    public Long getCurrentChatId() {


        if (updateEvent.hasMessage()) {
            return updateEvent.getMessage().getFrom().getId();
        }

        if (updateEvent.hasCallbackQuery()) {
            return updateEvent.getCallbackQuery().getFrom().getId();
        }

        return null;
    }

    /**
     * Метод для создания списка команд для бота
     * @param commands команды написанные в формате ( "/command", "Название/значение для пользователя" )
     */
    private void createCommands (String ... commands) {
        //Преобразование входных параметров (commands) в ArrayList BotCommands
        ArrayList<BotCommand> listCommands = new ArrayList<BotCommand>();

        for (int i = 0; i<commands.length; i+=2) {
            String key = commands [i];
            String description = commands [i+1];

            if (key.startsWith("/")) //Удаляем  "/"
                key = key.substring(1);

            BotCommand bc = new BotCommand(key, description);

            listCommands.add(bc);
        }
        var chId = getCurrentChatId();

        SetMyCommands cmds = new SetMyCommands();
        cmds.setCommands(listCommands);
        cmds.setScope(BotCommandScopeChat.builder().chatId(chId).build());
        executeTelegramApiMethod(cmds);

        //Показать кнопки меню
        var ex = new SetChatMenuButton();
        ex.setChatId(chId);
        ex.setMenuButton(MenuButtonCommands.builder().build());
        executeTelegramApiMethod(ex);

    }


    /**
     * Вместо стандартного execute(). Метод обеспечивает типобезопасное выполнение методов Telegram API и обработку возможных исключений.
     * @param method который необходимо выполнить
     * @return
     * @param <T>
     * @param <Method>
     */
    private <T extends Serializable, Method extends BotApiMethod<T>> T executeTelegramApiMethod(Method method) {
        try {
            return super.sendApiMethod(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Основной поток (метод)
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        bot.sendText(Long.valueOf(782908610L), "The SKRID_ONE_BOT is running!");  //The L just turns the Integer into a Long
    }

}
