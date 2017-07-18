

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * Created by ikownacki on 16.07.2017.
 */
public class GUI extends JFrame {
    private JPanel panel1;
    private JList<String> allCurrenciesList;
    private JList<String> selectedCurrenciesList;
    private JPanel panelWithChart2;
    private JPanel datePanel;


    private JDatePickerImpl datePicker;
    private DbHandler dbHandler;
    private TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();


    private DefaultListModel<String> allCurrenciesListModel = new DefaultListModel<>();
    private DefaultListModel<String> selectedCurrenciesListModel = new DefaultListModel<>();


    public GUI() {
        super();
        dbHandler = new DbHandler();


        dbHandler.connectDb();
        setContentPane(panel1);
        panel1.setVisible(true);

        initCurrenciesLists();
        initDatePicker();
        setAllCurrenciesListMouseListener();
        setSelectedCurrenciesListMouseListener();


        initChart();

        pack();
        this.setSize(1000, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initCurrenciesLists() {

        allCurrenciesList.setModel(allCurrenciesListModel);
        selectedCurrenciesList.setModel(selectedCurrenciesListModel);

        for (String currencyName : dbHandler.selectAllCurrencyCodes()) {
            allCurrenciesListModel.addElement(currencyName);
        }

        allCurrenciesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedCurrenciesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }


    private void initChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Kursy", "Data", "Cena (zł)", timeSeriesCollection);
        ChartPanel cp = new ChartPanel(chart);
        cp.setFillZoomRectangle(true);
        cp.setMouseWheelEnabled(true);
        cp.setVisible(true);
        panelWithChart2.add(cp);
    }

    private void initDatePicker() {

        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanelImpl = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanelImpl, new MyDateFormatter());
        datePanel.add(datePicker);
        datePicker.addActionListener(a -> {
            timeSeriesCollection.removeAllSeries();
            for (Object currencyCode : selectedCurrenciesListModel.toArray()) {
                getCurrencyData((String) currencyCode);
            }
        });
    }


    private void setAllCurrenciesListMouseListener() {
        allCurrenciesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    String currencyCode = allCurrenciesListModel.remove(index);
                    selectedCurrenciesListModel.addElement(currencyCode);
                    sortModel(selectedCurrenciesListModel);

                    getCurrencyData(currencyCode);

                }

            }
        });
    }

    private void setSelectedCurrenciesListMouseListener() {
        selectedCurrenciesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    String currencyCode = selectedCurrenciesListModel.remove(index);
                    allCurrenciesListModel.addElement(currencyCode);
                    sortModel(allCurrenciesListModel);

                    TimeSeries ts = timeSeriesCollection.getSeries(currencyCode);
                    timeSeriesCollection.removeSeries(ts);

                }
            }
        });
    }

    private void getCurrencyData(String currencyCode) {
        String dateFromDatePicker = datePicker.getJFormattedTextField().getText();
        if (dateFromDatePicker != null && !dateFromDatePicker.equals("")) {
            TimeSeries currencyData = new TimeSeries(currencyCode);
            LocalDate localDate = LocalDate.parse(dateFromDatePicker);
            for (CurrencyRepresentation cr : dbHandler.selectCurrenciesFromDb(currencyCode, localDate)) {
                LocalDate ld = cr.getPublicationDate();

                currencyData.add(new Day(ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear()), cr.getAverageExchangeRate());

                System.out.println(cr);
            }
            timeSeriesCollection.addSeries(currencyData);
        }
    }

    private void sortModel(DefaultListModel model) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            list.add((String) model.get(i));
        }
        Collections.sort(list);
        model.removeAllElements();
        for (String s : list) {
            model.addElement(s);
        }
    }

    public static void main(String[] args) {
        new GUI();
    }
}

