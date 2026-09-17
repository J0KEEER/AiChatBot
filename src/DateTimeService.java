import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service providing real-time dynamic date, day, hour, year, and time information.
 */
public class DateTimeService {

    private static final DateTimeFormatter TIME_12H = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_24H = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FULL = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private DateTimeService() {}

    /**
     * Returns formatted current time (both 12-hour and 24-hour).
     */
    public static String getCurrentTime() {
        ZonedDateTime now = ZonedDateTime.now();
        String formatted12 = now.format(TIME_12H);
        String formatted24 = now.format(TIME_24H);
        String zone = now.getZone().getId();
        return "The current time is " + formatted12 + " (" + formatted24 + " " + zone + ").";
    }

    /**
     * Returns the current hour in 12-hour and 24-hour formats.
     */
    public static String getCurrentHour() {
        LocalTime now = LocalTime.now();
        int hour24 = now.getHour();
        int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
        String amPm = hour24 < 12 ? "AM" : "PM";
        return "It is currently hour " + hour12 + " " + amPm + " (" + String.format("%02d:00", hour24) + " in 24h format).";
    }

    /**
     * Returns the day of the week and full date.
     */
    public static String getCurrentDay() {
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().name();
        String capitalizedDay = dayOfWeek.charAt(0) + dayOfWeek.substring(1).toLowerCase(Locale.ENGLISH);
        return "Today is " + capitalizedDay + " (" + today.format(DATE_FULL) + ").";
    }

    /**
     * Returns the current calendar date.
     */
    public static String getCurrentDate() {
        LocalDate today = LocalDate.now();
        return "Today's date is " + today.format(DATE_SHORT) + ".";
    }

    /**
     * Returns the current year.
     */
    public static String getCurrentYear() {
        int year = LocalDate.now().getYear();
        return "The current year is " + year + ".";
    }

    /**
     * Returns a full combined date and time summary.
     */
    public static String getDateTimeSummary() {
        ZonedDateTime now = ZonedDateTime.now();
        return "It is currently " + now.format(TIME_12H) + " on " + now.format(DATE_FULL) + ".";
    }
}
