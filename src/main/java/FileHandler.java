import java.util.ArrayList;
import java.util.List;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class FileHandler {
    private DbHandler dbHandler;
    private List<String> updatedFiles;

    public FileHandler() {
        dbHandler = new DbHandler();
        updatedFiles = new ArrayList<>();
    }

    public void init() {
        dbHandler.connectDb();
        updatedFiles = dbHandler.selectFileNamesFromDb();
    }

    public void processNewFile(String fileName, String filePath) {
        if (updatedFiles.contains(fileName)) {
            return;
        }
        List<CurrencyRepresentation> currencyRepresentationList = XMLReader.parseXmlToCurrencyRepList(filePath + fileName);
        if (currencyRepresentationList.isEmpty()) {
            return;
        }
        dbHandler.insertCurrenciesToDb(currencyRepresentationList);
        dbHandler.insertFileNameToDb(fileName);
        updatedFiles.add(fileName);
    }
}
