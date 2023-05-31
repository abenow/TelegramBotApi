package com.example.TelegramBotApi.service;

import com.example.TelegramBotApi.config.BotConfig;

import com.example.TelegramBotApi.model.User;
import com.example.TelegramBotApi.model.UserRepository;

import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Objects;


@Component

public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    UserRepository userRepository;
    private final BotConfig botConfig;
    private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class);


    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String RUB = "/rub";
    private static final String HELP = "/help";

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername(){
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken(){
        return botConfig.getToken();
    }
    @Override
    public void onUpdateReceived(Update update) {



        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();



        Long chatId = update.getMessage().getChatId();
        if (!message.isEmpty()){
            switch (message) {
                case START -> {
                    registeredUser(update.getMessage());
                    String userName = update.getMessage().getChat().getUserName();
                    startCommand(chatId, userName);
                }
                case USD,EUR,RUB -> RateCommand(chatId,message.replace("/", "").toUpperCase());
                case HELP -> helpCommand(chatId);
                default -> unknownCommand(chatId,message.replace("/", "").toUpperCase());
            }
        }



    }

    private void registeredUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

        }

    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                
                Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные Народным банком РК.
                
                Для этого воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                /rub - курс рубля   
                Также вы можете указать количество, пример:
                100USD       
                
                Дополнительные команды:
                /help - получение справки
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void RateCommand(Long chatId, String valuteName) {
        String formattedText;


        try {


            String currency = String.valueOf(CurrencyService.getCurrencyRate(valuteName));

            var text = "Курс " + valuteName + " на %s составляет %s тенге.";
            formattedText = String.format(text, LocalDate.now(), currency);




        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса", e);
            formattedText = "Не удалось получить текущий курс. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }



    private void helpCommand(Long chatId) {
        var text = """
                Справочная информация по боту
                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                /rub - курс рубля
                """;
        sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId,String valuteName) {
        String formattedText = "";

        Double valute = Double.valueOf(valuteName.replaceAll("[^0-9]", ""));
        String currancyName = valuteName.replaceAll("[^a-zA-Z]+","");

        Double currency = Objects.requireNonNull(CurrencyService.getCurrencyRate(currancyName));

        DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        Double result = valute*currency;

        String text;
        if(currency > 0) {
            text =  "Курс " + valuteName + " на %s составляет %s тенге.";
            formattedText = String.format(text, LocalDate.now(), decimalFormat.format(result));

        }else {
            text = "Не удается распознать комманду!";
            formattedText = String.format(text, LocalDate.now(), decimalFormat.format(result));
        }
        sendMessage(chatId, formattedText);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }
        catch (TelegramApiException e){

        }
    }

}