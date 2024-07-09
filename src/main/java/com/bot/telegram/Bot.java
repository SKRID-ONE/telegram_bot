package com.bot.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.validation.UnexpectedTypeException;
import java.util.List;
import java.util.Locale;

public class Bot extends TelegramLongPollingBot {

    //Переменные
    private boolean screaming = false; //Переменная для выбора режима ответа бота: Крик/тихо

    //Клавиатуры (меню)
    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;

    //Кнопки
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

        //Клавиатуры
        keyboardM1 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next))
                .build();
        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back))
                .keyboardRow(List.of(url))
                .build();

        // Если боту отправили сообщение
        if (update.hasMessage()){
            //Сначала надо обработать все команды бота /commands
            //Получение данных: сообщение, отправитель и его id
            var msg = update.getMessage();
            var user = msg.getFrom();
            var id = user.getId();
            //Предполагается, что мы знаем команды бота
            if(msg.getText().equals("/scream")){
                screaming = true;
            } else if (msg.getText().equals("/whisper")) {
                screaming = false;
            } else if (msg.getText().equals("/menu")){
                sendMenu(id,"<b>Menu 1</b>", keyboardM1);
            }


            //Логика поведения бота
            if(screaming){
                scream(id, msg);
            } else {
                copyMessage(id, msg.getMessageId());
            }

            return; //Мы не хотим повторять команды - выходим

            //Если нажали на кнопку
        } else if (update.hasCallbackQuery()) {
//            System.out.println("CALLBACK");
            //CallBack запрос
            CallbackQuery callbackQuery = update.getCallbackQuery();

            Long id = callbackQuery.getMessage().getChatId();
            String queryId = callbackQuery.getId();
            String data = callbackQuery.getData();
            int msgId = callbackQuery.getMessage().getMessageId();

            //Метод для обработки нажатия кнопок
            buttonTap(id, queryId, data, msgId);

//            AnswerCallbackQuery close = AnswerCallbackQuery.builder()
//                    .callbackQueryId(update.getCallbackQuery().getId()).build();
//
//            try {
//                execute(close);
//            } catch (TelegramApiException e) {
//                throw new RuntimeException(e);
//            }

        }




    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
        //System.out.println(updates);
    }


    //Методы

    /**
     * Отправка сообщения
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

    //Основной поток (метод)
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        bot.sendText(Long.valueOf(782908610L), "The SKRID_ONE_BOT is running!");  //The L just turns the Integer into a Long

    }

}
