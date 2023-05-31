package com.example.TelegramBotApi.service;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CurrencyService {

    public static Double getCurrencyRate(String message) {

        switch (message) {
            case "RUB", "EUR", "USD" -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String formattedDate = LocalDate.now().format(formatter);
                String url = "https://nationalbank.kz/rss/get_rates.cfm?fdate=" + formattedDate;
                try {
                    Document jdomDocument = createJDOMusingURL(url);
                    Element root = jdomDocument.getRootElement();
                    List<Element> exchangeRatesListElements = root.getChildren("item");
                    Double rate = null;

                    for (Element exchangeRatesEl : exchangeRatesListElements) {
                        String title = exchangeRatesEl.getChildText("title");
                        if (title.equalsIgnoreCase(message)) {
                            rate = Double.parseDouble(exchangeRatesEl.getChildText("description"));
                            break;
                        }
                    }
                    return rate;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            default -> {
                return 0.0;
            }
        }

        return null;
    }

    private static Document createJDOMusingURL(String url) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new URL(url));
    }
}










