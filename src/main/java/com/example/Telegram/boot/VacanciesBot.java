package com.example.Telegram.boot;

import com.example.Telegram.boot.dto.VacancyDto;

import com.example.Telegram.boot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {

    @Autowired
    private VacancyService vacancyService;

    private final Map<Long,String> lastShowVacanciesLevel = new HashMap<>();

    public VacanciesBot () {
        super("6302263602:AAHTkFRJF_fv_mEv50RoyJDRDNE1EEDfvYY");
    }
    //6302263602:AAHTkFRJF_fv_mEv50RoyJDRDNE1EEDfvYY

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.getMessage() != null) {
                handleStartCommand(update);
            }

            if (update.getCallbackQuery() != null) {
                String callBackData = update.getCallbackQuery().getData();

                if ("showJuniorVacancies".equals(callBackData)) {
                    showJuniorVacancies(update);
                }else if ("showMiddleVacancies".equals(callBackData)){
                    showMiddleVacancies(update);
                }else if ("showSeniorVacancies".equals(callBackData)) {
                    showSeniorVacancies(update);
                }else if (callBackData.startsWith("VacancyId=")) {
                    String id = callBackData.split("=")[1];
                    showVacancyDescription(id,update);
                } else  if ("backToVacancies".equals(callBackData)) {
                    handleBackToVacanciesCommand(update);
                } else if ("backToStartMenu".equals(callBackData)) {
                    handleBackToStartMenu(update);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Cant sent message to user",e);
        }
    }

    private void handleBackToStartMenu (Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose title:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }
    private void handleBackToVacanciesCommand (Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String level = lastShowVacanciesLevel.get(chatId);

        if ("junior".equals(level)) {
            showJuniorVacancies(update);
        } else if("middle".equals(level)) {
            showMiddleVacancies(update);
        } else if ("senior".equals(level)) {
            showSeniorVacancies(update);
        }
    }

    private void showVacancyDescription (String id,Update update) throws TelegramApiException {
        VacancyDto vacancyDto = vacancyService.get(id);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        String vacancyInfo = """
                *Title:* %s
                *Company:* %s
                *Short Description:* %s
                *Description:* %s
                *Salary:* %s
                *Link:* [%s](%s)
                """.formatted(
                        escapeMarkdownReserveChars(vacancyDto.getTitle()),
                escapeMarkdownReserveChars(vacancyDto.getCompany()),
                escapeMarkdownReserveChars(vacancyDto.getShortDescription()),
                escapeMarkdownReserveChars(vacancyDto.getLongDescription()),
                vacancyDto.getSalary().isBlank() ? "Not specified" : escapeMarkdownReserveChars(vacancyDto.getSalary()),
                "Click here for details",
                escapeMarkdownReserveChars(vacancyDto.getLink())
        );
        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);
    }

    private String escapeMarkdownReserveChars(String text) {
        return text.replace("-","\\-")
                .replace("_","\\_")
                .replace("*","\\*")
                .replace("[","\\[")
                .replace("]","\\]")
                .replace("(","\\(")
                .replace(")","\\)")
                .replace("~","\\~")
                .replace("`","\\`")
                .replace(">","\\>")
                .replace("#","\\#")
                .replace("+","\\+")
                .replace(".","\\.")
                .replace("!","\\!");
    }

    private ReplyKeyboard getBackToVacanciesMenu () {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenu = new InlineKeyboardButton();
        backToStartMenu.setText("Back to start menu");
        backToStartMenu.setCallbackData("backToStartMenu");
        row.add(backToStartMenu);

        InlineKeyboardButton getChatGptButton = new InlineKeyboardButton();
        getChatGptButton.setText("Get cover letter");
        getChatGptButton.setUrl("https://chat.openai.com/");
        row.add(getChatGptButton);

        return new InlineKeyboardMarkup(List.of(row));
    }

    private void showJuniorVacancies (Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy: ");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);

        lastShowVacanciesLevel.put(chatId,"junior");

    }private void showMiddleVacancies (Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy: ");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);

        lastShowVacanciesLevel.put(chatId,"middle");
    }private void showSeniorVacancies (Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy: ");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());
        execute(sendMessage);

        lastShowVacanciesLevel.put(chatId,"senior");
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();
        return getVacanciesMenu(vacancies);
    }
    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getMiddleVacancies();
        return getVacanciesMenu(vacancies);

    }private ReplyKeyboard getSeniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getSeniorVacancies();
        return getVacanciesMenu(vacancies);
    }

    private ReplyKeyboard getVacanciesMenu (List <VacancyDto> vacancies) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (VacancyDto vacancy : vacancies) {
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("VacancyId=" + vacancy.getId());
            row.add(vacancyButton);
        }


        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }
    private void handleStartCommand(Update update) {
        String text = update.getMessage().getText();
        System.out.println("Received text "+text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Welcome to vacancies bot! Please chose your tittle");
        sendMessage.setReplyMarkup(getStartMenu());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior");
        junior.setCallbackData("showJuniorVacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("Middle");
        middle.setCallbackData("showMiddleVacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("Senior");
        senior.setCallbackData("showSeniorVacancies");
        row.add(senior);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    @Override
    public String getBotUsername() {
        return "ViktorVBotVacancies";
    }

//    public void setVacancyService(VacancyService vacancyService) {
//        this.vacancyService = vacancyService;
//    }
}
