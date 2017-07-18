import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class DbHandler {


    private Connection connection;

    private  final String dBConnectionUrl;
    private  final String dBUser;
    private  final String dBUserPassword;

    private static final String INSERT_CURRENCIES =
            "INSERT INTO tab_kursy_nbp (id, currencycode, currencymultiplier, averageexchangerate, publicationdate) " +
                    "VALUES (nextval('seq_kursy_nbp'), ?,?,?,?)";

    private static final String INSERT_FILE_NAME = "INSERT INTO tab_nazwy_plikow (id, filename) " +
            "VALUES (nextval('seq_nazwy_plikow'), ?)";

    private static final String SELECT_CURRENCY_BY_CODE = "SELECT  * FROM tab_kursy_nbp WHERE currencycode=? AND  publicationdate >= ?";

    private static final String SELECT_ALL_CURRENCIES_CODES = "SELECT DISTINCT  currencycode FROM  tab_kursy_nbp ORDER BY currencycode";

    private static final String SELECT_FILE_NAMES = "SELECT * FROM tab_nazwy_plikow";

    private static final String CURRENCY_CODE = "currencycode";
    private static final String CURRENCY_MULTIPLER = "currencymultiplier";
    private static final String AVAREGE_EXCHANGE_RATE = "averageexchangerate";
    private static final String PUBLICATION_DATE = "publicationdate";
    private static final String FILE_NAME = "filename";

    public DbHandler() {
        Properties properties = new Properties();

        try {
            InputStream inputStream = new FileInputStream("target/classes/config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        dBConnectionUrl = properties.getProperty("dBConnectionUrl");
        dBUser = properties.getProperty("dBUser");
        dBUserPassword = properties.getProperty("dBUserPassword");


    }

    public boolean connectDb() {
        try {
            this.connection = DriverManager.getConnection(dBConnectionUrl, dBUser, dBUserPassword);
            return true;
        } catch (SQLException e) {
            connection = null;
            System.out.println(e.toString());
            return false;
        }

    }

    public boolean disconnectDb() {
        try {
            connection.close();
            connection = null;
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
        finally {
            connection = null;
        }
    }

    public void insertCurrenciesToDb(List<CurrencyRepresentation> currencyList) {
        checkConnectionAndReconnect();
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CURRENCIES)) {

            for (CurrencyRepresentation cr : currencyList) {
                statement.setString(1, cr.getCurrencyCode());
                statement.setInt(2, cr.getCurrencyMultiplier());
                statement.setDouble(3, cr.getAverageExchangeRate());
                statement.setDate(4, Date.valueOf(cr.getPublicationDate()));

                statement.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertFileNameToDb(String fileName) {
        checkConnectionAndReconnect();
        try (PreparedStatement statement = connection.prepareStatement(INSERT_FILE_NAME)) {
            statement.setString(1, fileName);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CurrencyRepresentation> selectCurrenciesFromDb(String currencyCode, LocalDate dateFrom) {
        List<CurrencyRepresentation> currencyList = new ArrayList<>();
        checkConnectionAndReconnect();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_CURRENCY_BY_CODE)) {
            statement.setString(1,currencyCode);
            statement.setDate(2,Date.valueOf(dateFrom));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                CurrencyRepresentation cr = new CurrencyRepresentation(
                        resultSet.getString(CURRENCY_CODE),
                        resultSet.getInt(CURRENCY_MULTIPLER),
                        resultSet.getDouble(AVAREGE_EXCHANGE_RATE),
                        resultSet.getDate(PUBLICATION_DATE).toLocalDate());

                currencyList.add(cr);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencyList;
    }

    public List<String> selectAllCurrencyCodes(){
        List<String> currencycodes = new ArrayList<>();
        checkConnectionAndReconnect();
        try(Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(SELECT_ALL_CURRENCIES_CODES);
            while (resultSet.next()){
                currencycodes.add(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencycodes;
    }

    public List<String> selectFileNamesFromDb() {
        List<String> fileNames = new ArrayList<>();
        checkConnectionAndReconnect();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_FILE_NAMES)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fileNames.add(resultSet.getString(FILE_NAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    private void checkConnectionAndReconnect(){
        try {
            if (connection.isValid(5)) {
                return;
            } else {
                this.disconnectDb();
                this.connectDb();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

}
