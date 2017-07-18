import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class FileDownloader {
    private boolean isFirstDownload;
    private final String targetPathString;

    private final String basicUrl = "http://www.nbp.pl/kursy/xml/";
    private final String dir = "dir";
    private final String txt = ".txt";
    private final String xml = ".xml";
    private final int startYear;
    private final String lastA = "LastA.xml";

    public FileDownloader(String targetPathString, int startYear) {
        this.startYear = startYear;
        this.targetPathString = targetPathString;
        isFirstDownload = true;
    }

    public void downloadFiles() {

        if (isFirstDownload) {
            downloadHistoricalFiles();
        } else if (!isFirstDownload) {
            downloadActualFile();
        }

    }

    private void downloadHistoricalFiles() {
        int currYear = Year.now().getValue();

        for (int i = startYear; i < currYear; i++) {
            String dirUrl = basicUrl + dir + i + txt;
            downloadFilesFromDirfile(dirUrl);
        }
        String dirUrl = basicUrl + dir + txt;
        downloadFilesFromDirfile(dirUrl);

        isFirstDownload = false;
    }

    private void downloadFilesFromDirfile(String urlString) {
        try {
            URL url = new URL(urlString);
            Scanner scanner = new Scanner(url.openStream());

            while (scanner.hasNext()) {
                String fileName = scanner.nextLine();
                if (fileName.startsWith("a")) {
                    URL xmlFileUrl = new URL(basicUrl + fileName + xml);
                    InputStream inputStream = xmlFileUrl.openStream();
                    Path newXmlFilePath = Paths.get(targetPathString + fileName + xml);
                    Files.copy(inputStream, newXmlFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadActualFile() {
        try {
            URL xmlFileUrl = new URL(basicUrl + lastA);
            String newFileName = XMLReader.buildNewFileName(xmlFileUrl);
            InputStream inputStream = xmlFileUrl.openStream();
            Path newXmlFilePath = Paths.get(targetPathString + newFileName + xml);
            Files.copy(inputStream, newXmlFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String downloadsDir = null;
        Integer startYear = null;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("target/classes/config.properties"));
            downloadsDir = properties.getProperty("downloadsDir");
            startYear = Integer.valueOf(properties.getProperty("startYear"));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        FileDownloader fileDownloader = new FileDownloader(downloadsDir, startYear);
        Runnable runnableDownloader = fileDownloader::downloadFiles;

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(runnableDownloader, 0, 24, TimeUnit.HOURS);


    }

}
