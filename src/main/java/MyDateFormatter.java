import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ikownacki on 18.07.2017.
 */
public class MyDateFormatter extends JFormattedTextField.AbstractFormatter {

    private String pattern = "yyy-MM-dd";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return simpleDateFormat.parse(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return simpleDateFormat.format(cal.getTime());
        }

        return "";
    }
}
