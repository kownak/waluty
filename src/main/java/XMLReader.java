import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class XMLReader {
    private static final String TABLE_NUMBER = "numer_tabeli";
    private static final String POSITION = "pozycja";
    private static final String PUBLICATION_DATE = "data_publikacji";
    private static final String CURRENCY_CODE = "kod_waluty";
    private static final String CURRENCY_EXCHANGE_RATE = "kurs_sredni";
    private static final String CURRENCY_MULTIPLER = "przelicznik";


    public static String buildNewFileName(URL url) {
        String newFileName = "";

        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(url);
            Element rootElement = document.getRootElement();
            String tabNumber = "";
            String pubDate = "";
            for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext(); ) {
                Element element = iterator.next();
                String elementName = element.getName();

                if (elementName.equals(TABLE_NUMBER)) {
                    tabNumber = (element.getStringValue()).split("/")[0];
                } else if (elementName.equals(PUBLICATION_DATE)) {
                    String tmp[] = element.getStringValue().split("-");
                    pubDate = tmp[0].substring(2) + tmp[1] + tmp[2];
                }
                if (!tabNumber.equals("") && !pubDate.equals("")) {
                    break;
                }
            }

            newFileName = "a" + tabNumber + "z" + pubDate;

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return newFileName;
    }

    public static List<CurrencyRepresentation> parseXmlToCurrencyRepList(String pathToXml) {
        ArrayList<CurrencyRepresentation> currencyRepList = new ArrayList<>();

        try {
            Document document = parse(pathToXml);
            Element rootElement = document.getRootElement();
            LocalDate publicationDate = null;

            for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext(); ) {
                Element element = iterator.next();
                String elementName = element.getName();

                if (elementName.equals(POSITION)) {

                    currencyRepList.add(buildCurrentRepresentation(element, publicationDate));

                } else if (elementName.equals(PUBLICATION_DATE)) {
                    String dateFromXML = element.getStringValue();
                    publicationDate = LocalDate.parse(dateFromXML);
                }
            }
        } catch (DocumentException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } finally {

        }

        return currencyRepList;
    }

    private static Document parse(String pathToXml) throws DocumentException {
        File xmlFile = new File(pathToXml);
        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFile);
        return document;
    }

    private static CurrencyRepresentation buildCurrentRepresentation(Element parentElement, LocalDate publicationDate) {
        String currencyCode = null;
        Double currencyExchangeRate = null;
        Integer currencyMultiplier = null;

        for (Iterator<Element> iterator = parentElement.elementIterator(); iterator.hasNext(); ) {
            Element childElement = iterator.next();
            String childElementName = childElement.getName();

            if (childElementName.equals(CURRENCY_CODE)) {
                currencyCode = childElement.getStringValue();

            } else if (childElementName.equals(CURRENCY_EXCHANGE_RATE)) {
                String tmpStr = childElement.getStringValue().replaceAll(",", ".");
                currencyExchangeRate = Double.parseDouble(tmpStr);

            } else if (childElementName.equals(CURRENCY_MULTIPLER)) {
                String tmpStr = childElement.getStringValue();
                currencyMultiplier = Integer.valueOf(tmpStr);
            }
        }

        return new CurrencyRepresentation(currencyCode, currencyMultiplier, currencyExchangeRate, publicationDate);
    }


}
