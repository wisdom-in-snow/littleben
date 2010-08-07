package littleben;

import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author Simon
 */
public class Main {

    static Settings settings = new Settings();
    static TrayIcon trayIcon;
    static Timer timer;
    static TimerTask timertask;
    static Image enabledImage,  disabledImage;

    static String time(int t) {

        if (t > 11) {
            t %= 12;
            return (t == 0) ? "midday" : t + "pm";
        } else {
            return (t == 0) ? "midnight" : t + "am";
        }
    }

    static void reschedule()
    {
        GregorianCalendar next;
        next = new GregorianCalendar();
        int freq = settings.frequency;
        int mins = next.get(GregorianCalendar.MINUTE);
        mins = (mins / freq + 1) * freq;
        next.set(GregorianCalendar.MINUTE, mins);
        next.set(GregorianCalendar.SECOND, 0);
        next.set(GregorianCalendar.MILLISECOND, 0);

        if (timer != null) timer.cancel();
        timertask = new TimerTask()
        {
            public void run()
            {
                GregorianCalendar cal;
                long now, start, end;
                cal = new GregorianCalendar();
                now = cal.getTimeInMillis();
                // start time
                cal.set(GregorianCalendar.HOUR_OF_DAY, settings.startTime);
                cal.set(GregorianCalendar.MINUTE, 0);
                cal.set(GregorianCalendar.SECOND, 0);
                start = cal.getTimeInMillis();
                // end time
                cal.set(GregorianCalendar.HOUR_OF_DAY, settings.endTime);
                end = cal.getTimeInMillis();

                // compare
                if (now >= start && (end < start || now <= end))
                {
                    String message = new Date().toString();
                    System.out.println("Alert: " + message);
                    trayIcon.displayMessage("Alert!", message,
                            TrayIcon.MessageType.WARNING);
                }
                else
                {
                    System.err.println("Skipping alert");
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timertask, next.getTime(), freq * 60 * 1000);
        String tooltip = "Next alert at " + next.getTime().toString();
        System.out.println(tooltip);
        trayIcon.setToolTip(tooltip);
    }

    static ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            //System.err.println(action + e);
            if (action == null) {
                return;
            } else if (action.equals("Every hour")) {
                settings.frequency = 60;
                reschedule();
            } else if (action.equals("Every half an hour")) {
                settings.frequency = 30;
                reschedule();
            } else if (action.equals("Every 15 minutes")) {
                settings.frequency = 15;
                reschedule();
            } else if (action.equals("Every 2 minutes")) {
                settings.frequency = 2;
                reschedule();
            } else if (action.equals("All day")) {
                settings.startTime = 0;
                settings.endTime = 24;
            } else if (action.equals("Morning")) {
                settings.startTime = 6;
                settings.endTime = 12;
            } else if (action.equals("Afternoon")) {
                settings.startTime = 12;
                settings.endTime = 18;
            } else if (action.equals("Overnight")) {
                settings.startTime = 18;
                settings.endTime = 6;
            } else if (action.equals("Exit")) {
                System.exit(0);
            } else {
                trayIcon.displayMessage("Error", "Unknown action: " + action,
                        TrayIcon.MessageType.ERROR);
                return;
            }
            //update();
        }
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null,
                    "This computer does not support the system tray.",
                    "System Tray Unsupported", JOptionPane.ERROR_MESSAGE);
        } else {
            // init images & sound
            URL url = Main.class.getResource("enabled.jpg");
            enabledImage = Toolkit.getDefaultToolkit().getImage(url);
            // Popup menu
            PopupMenu popup;
            Menu menu;
            MenuItem item;
            popup = new PopupMenu();
            // Alert Submenu
            menu = new Menu("Alert");
            item = new MenuItem("Every hour");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Every 45 minutes");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Every half an hour");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Every 15 minutes");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Every 2 minutes");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Never");
            item.addActionListener(listener);
            menu.add(item);
            popup.add(menu);
            // Active submenu
            menu = new Menu("Active");
            item = new MenuItem("All day");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Morning");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Afternoon");
            item.addActionListener(listener);
            menu.add(item);
            item = new MenuItem("Overnight");
            item.addActionListener(listener);
            menu.add(item);
            popup.add(menu);
            popup.addSeparator();
            item = new MenuItem("Exit");
            item.addActionListener(listener);
            popup.add(item);
            // setup tray
            trayIcon = new TrayIcon(enabledImage, "...", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(listener);
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
            reschedule();
        }
    }
}
