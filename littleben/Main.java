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
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Clip;
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
//    static Clip clip;

    static String time(int t) {

        if (t > 11) {
            t %= 12;
            return (t == 0) ? "midday" : t + "pm";
        } else {
            return (t == 0) ? "midnight" : t + "am";
        }
    }

    static void update() {
        // disable previous timer.
        if (timertask != null) {
            timertask.cancel();
        }
        String message;
        if (settings.enabled) {
            message = "Alerting every " + settings.frequency + " minutes from " + time(settings.startTime) + " to " + time(settings.endTime);
            schedule();
        } else {
            message = "Alerts disabled";
            trayIcon.setToolTip(message);
        }
        System.out.println(message);
        trayIcon.displayMessage(null, message, TrayIcon.MessageType.WARNING);
        trayIcon.setImage(settings.enabled ? enabledImage : disabledImage);
        settings.save();
    }

    static void schedule() {
        GregorianCalendar next, start, end;
        // calculate next alert time
        next = new GregorianCalendar();
        int freq = settings.frequency;
        int mins = next.get(GregorianCalendar.MINUTE);
        mins = (mins / freq + 1) * freq;
        next.set(GregorianCalendar.MINUTE, mins);
        next.set(GregorianCalendar.SECOND, 0);
        next.set(GregorianCalendar.MILLISECOND, 0);
        // start time
        start = (GregorianCalendar) next.clone();
        start.set(GregorianCalendar.HOUR_OF_DAY, settings.startTime);
        start.set(GregorianCalendar.MINUTE, 0);
        // end time
        end = (GregorianCalendar) next.clone();
        end.set(GregorianCalendar.HOUR_OF_DAY, settings.endTime);
        end.set(GregorianCalendar.MINUTE, 0);
        // range check
        if (start.before(end)) {
            if (next.before(start)) {
                next = start;
            } else if (next.after(end)) {
                next = start;
                next.add(GregorianCalendar.DAY_OF_MONTH, 1);
            }
        } else { // end.before(start)
            if (next.after(end) && next.before(start)) {
                next = start;
            }
        }
        // set new timer
        timertask = new TimerTask() {

            public void run() {
                String message = new Date().toString();
                System.out.println("Alert: " + message);
                trayIcon.displayMessage("Alert!", message,
                        TrayIcon.MessageType.WARNING);
//                if (settings.sound) {
//                    clip.start();
//                }
                schedule();
            }
        };
        timer = new Timer();
        timer.schedule(timertask, next.getTime(), freq * 60 * 1000);
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
                settings.enabled = true;
            } else if (action.equals("Every 45 minutes")) {
                settings.frequency = 45;
                settings.enabled = true;
            } else if (action.equals("Every half an hour")) {
                settings.frequency = 30;
                settings.enabled = true;
            } else if (action.equals("Every 15 minutes")) {
                settings.frequency = 15;
                settings.enabled = true;
            } else if (action.equals("Every 2 minutes")) {
                settings.frequency = 2;
                settings.enabled = true;
            } else if (action.equals("Never")) {
                settings.enabled = false;
            } else if (action.equals("All day")) {
                settings.startTime = 0;
                settings.endTime = 24;
            } else if (action.equals("Morning")) {
                settings.startTime = 0;
                settings.endTime = 12;
            } else if (action.equals("Afternoon")) {
                settings.startTime = 12;
                settings.endTime = 18;
            } else if (action.equals("Overnight")) {
                settings.startTime = 18;
                settings.endTime = 6;
//            } else if (action.equals("Enabled")) {
//                settings.sound = true;
//            } else if (action.equals("Disabled")) {
//                settings.sound = false;
            } else if (action.equals("Exit")) {
                System.exit(0);
            } else {
                trayIcon.displayMessage("Error", "Unknown action: " + action,
                        TrayIcon.MessageType.ERROR);
                return;
            }
            update();
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
            url = Main.class.getResource("disabled.jpg");
            disabledImage = Toolkit.getDefaultToolkit().getImage(url);
//            url = Main.class.getResource("toll.wav");
//            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
//            clip = AudioSystem.getClip();
//            clip.open(audioIn);
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
//            item = new MenuItem("Every 2 minutes");
//            item.addActionListener(listener);
//            menu.add(item);
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
//            // Sound
//            menu = new Menu("Sound");
//            item = new MenuItem("Enabled");
//            item.addActionListener(listener);
//            menu.add(item);
//            item = new MenuItem("Disabled");
//            item.addActionListener(listener);
//            menu.add(item);
//            popup.add(menu);
            popup.addSeparator();
            item = new MenuItem("Exit");
            item.addActionListener(listener);
            popup.add(item);
            // setup tray
            trayIcon = new TrayIcon(settings.enabled ? enabledImage : disabledImage,
                    "disabled...", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(listener);
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
            update();
        }
    }
}
