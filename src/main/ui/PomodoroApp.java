package ui;

import javax.swing.*;
import java.awt.*;

import model.Event;
import model.EventLog;
import model.PomodoroSession;
import model.Statistics;
import model.Task;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.Timer;

// Represents the user interface for the Pomodoro application.
// Allows users to set up work sessions, take breaks, and manage tasks.
public class PomodoroApp {
    private JFrame frame;
    private JTextField taskField;
    private JButton addTaskButton;
    private JButton saveButton;
    private JButton loadButton;
    private JTextArea taskListArea;
    private JLabel timerLabel;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskListView; //for tasks
    private JButton markCompletedButton; //for tasks
    //    private JTextField workField;
//    private JTextField shortBreakField;
//    private JTextField longBreakField;
    private PomodoroSession session;
    private Statistics statistics; //has completed task
    private List<Task> taskList; //uncompleted task
    private final Scanner input;
    boolean keepGoing = true;
    private static final String JSON_STORE = "./data/pomodoro.json";
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;
    private Timer sessionMonitorTimer;
    private boolean wasOnBreak;

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the input scanner and runs the application.
     */
    public PomodoroApp() {
        initializeUI();
        input = new Scanner(System.in);
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        runApp();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets up the frame and its components for the Pomodoro application.
     */
    private void initializeUI() {
        frame = new JFrame("Pomodoro App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout()); // setting for layout
        taskList = new ArrayList<>();

        initializeTaskInputComponents();
        initializeTaskListView();
        initializeStatisticsButton();
        initializeSaveButton();
        initializeLoadButton();

        // Initialize timer-related components
        initializeTimerLabel();
        initializeResetTimerButton();
        initializeStopTimerButton();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onApplicationExit(); // イベントログを出力
                System.exit(0); // アプリケーションを終了
            }
        });

        frame.setVisible(true); // show GUI
    }

    /*
     * MODIFIES: this
     * EFFECTS: Updates the timer label to immediately reflect the current time of the session.
     */
    private void updateTimerLabelImmediately() {
        if (session != null && timerLabel != null) {
            int currentTime = session.getCurrentDuration();
            int minutes = currentTime / 60;
            int seconds = currentTime % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    /*
     * EFFECTS: If a session exists, displays the current settings of the Pomodoro session in a dialog window.
     */
    private void showCurrentSettings() {
        if (session != null) {
            String settingsMessage
                    = String.format("Current settings:\nWorking time: %d sec\nShort Break: %d sec\nLong Break: %d sec",
                    session.getWorkDuration(),
                    session.getShortBreakDuration(),
                    session.getLongBreakDuration());
            JOptionPane.showMessageDialog(frame, settingsMessage, "Current settings", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Starts or restarts the sessionMonitorTimer to update the timerLabel every second
     *          with the current time from the session.
     *          If a sessionMonitorTimer is already running, it is first cancelled.
     */
    private void updateTimerLabel() {
        if (sessionMonitorTimer != null) {
            sessionMonitorTimer.cancel();
        }
        sessionMonitorTimer = new Timer();
        sessionMonitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session != null && timerLabel != null) {
                    int currentTime = session.getCurrentDuration();
                    int minutes = currentTime / 60;
                    int seconds = currentTime % 60;
                    SwingUtilities.invokeLater(() -> timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
                }
            }
        }, 0, 1000);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the components for task input, including the taskField and addTaskButton.
     *          Adds these components to the frame.
     */
    private void initializeTaskInputComponents() {
        // Initialization of text fields and buttons for task entry
        // Text field for task input
        taskField = new JTextField(20);
        frame.add(taskField);

        // Add Task button
        addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> addTask());
        frame.add(addTaskButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the components for displaying the task list, including the taskListModel, taskListView,
     *          and markCompletedButton. Adds these components to the frame.
     */
    private void initializeTaskListView() {
        // Initialization of task list view
        // Task List Models and Views
        taskListModel = new DefaultListModel<>();
        taskListView = new JList<>(taskListModel);
        taskListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(taskListView);
        listScrollPane.setPreferredSize(new Dimension(250, 150));
        frame.add(listScrollPane);

        // Done mark button
        markCompletedButton = new JButton("Mark as Completed");
        markCompletedButton.addActionListener(e -> markTaskAsCompleted());
        frame.add(markCompletedButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the statistics display button to the frame.
     *          Sets up the action listener for the button.
     */
    private void initializeStatisticsButton() {
        // Initialize Statistics Display Button
        // stat button
        JButton statsButton = new JButton("View Statistics");
        statsButton.addActionListener(e -> showStatistics());
        frame.add(statsButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the save session button to the frame.
     *          Sets up the action listener for the button to save the current session state to a file.
     */
    private void initializeSaveButton() {
        // saving button
        saveButton = new JButton("Save Session");
        saveButton.addActionListener(e -> {
            try {
                jsonWriter.open();
                jsonWriter.write(session, taskList);
                jsonWriter.close();
                JOptionPane.showMessageDialog(frame, "Session successfully saved.",
                        "Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(frame, "Could not save to file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.add(saveButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the load session button to the frame.
     *          Sets up the action listener for the button to load a session state from a file.
     */
    private void initializeLoadButton() {
        // initialize loading button
        loadButton = new JButton("Load Session");
        loadButton.addActionListener(e -> loadSession());
        frame.add(loadButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Loads the Pomodoro session and task list from a file.
     *          Updates session, statistics, and task list based on the loaded data.
     *          If the session is running, starts the timer and updates the timer label.
     *          Notifies the user about the success or failure of the operation.
     */
    private void loadSession() {
        // Execution of load processing
        try {
            session = jsonReader.readPomodoroSession();
            statistics = session.getStatistics(); // Set the latest Statistics
            taskList = jsonReader.readTasks();
            updateTaskListModel();
            if (session.isRunning()) {
                session.startTimer();
                updateTimerLabelImmediately(); // Added method to immediately update timer labels
            }
            JOptionPane.showMessageDialog(frame, "Session loaded successfully.",
                    "Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to read from file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Updates the task list model with the current tasks and their completion status.
     */
    private void updateTaskListModel() {
        // Update task list model
        taskListModel.clear(); // Clear the existing list first.
        List<Task> completedTasks = statistics.getCompletedTaskList();
        for (Task task : completedTasks) {
            String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
            taskListModel.addElement(task.getTaskName() + taskStatus); // Add new task
        }
        for (Task task : taskList) {
            String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
            taskListModel.addElement(task.getTaskName() + taskStatus); //Add new task
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the timer label to the frame.
     */
    private void initializeTimerLabel() {
        // Initialization of timer labels
        timerLabel = new JLabel("00:00");
        frame.add(timerLabel);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the reset timer button to the frame.
     *          Sets up the action listener for the button to reset the timer.
     */
    private void initializeResetTimerButton() {
        // Reset timer button initialization
        JButton resetTimerButton = new JButton("Reset Timer");
        resetTimerButton.addActionListener(e -> resetTimer());
        frame.add(resetTimerButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Resets the timer of the current session.
     *          Asks the user to either continue or exit the application after the reset.
     */
    private void resetTimer() {
        if (session == null) {
            return; // Early Return
        }

        session.resetTimer();
        cancelExistingTimer(); // Separate timer cancellation into separate methods

        int result = JOptionPane.showOptionDialog(frame, "The timer has been reset. What should I do?",
                "Reset Timer", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"continue", "end"}, "continue");

        if (result == JOptionPane.YES_OPTION) {
            continueAfterReset();
        } else {
            onApplicationExit();
            System.exit(0);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Cancels the existing session monitor timer if it is running.
     */
    private void cancelExistingTimer() {
        if (sessionMonitorTimer != null) {
            sessionMonitorTimer.cancel();
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Restarts the work session and updates the timer label after a reset.
     *          Displays the current settings to the user.
     */
    private void continueAfterReset() {
        session.startWork();
        updateTimerLabel();
        startSessionMonitor();
        showCurrentSettings();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds the stop timer button to the frame.
     *          Sets up the action listener for the button to stop the timer.
     */
    private void initializeStopTimerButton() {
        // Initialize stop timer button
        JButton stopTimerButton = new JButton("Stop Timer");
        stopTimerButton.addActionListener(e -> stopTimer());
        frame.add(stopTimerButton);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Stops the current timer and session.
     *          Prompts the user to either resume the session or exit the application.
     */
    private void stopTimer() {
        // Stop Timer Button Action
        if (session != null) {
            session.stop();

            int result = JOptionPane.showOptionDialog(
                    frame,
                    "The timer has been stopped. What should I do?",
                    "Timer stop",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Resume", "Exit"},
                    "Resume"
            );

            if (result == JOptionPane.YES_OPTION) {
                // resume
                keepGoing = true;
                session.startTimer();
                updateTimerLabel();
                startSessionMonitor();
                show(taskList.size());
            } else {
                // When leaving
                onApplicationExit();
                System.exit(0);
            }
        }
    }

    /*
     * EFFECTS: Displays a modal dialog with a fun image and a message instructing
     *          the user to close the dialog to start work.
     *          The dialog is centered relative to the main application frame.
     *          The dialog is set to be modal, which blocks user input to other
     *          windows of the application until it is closed.
     *          The image is loaded from the 'ui' package within the classpath.
     */
    private void showFunImage() {
        // Load the image from the 'ui' package
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/ui/image.jpeg"));
        JLabel imageLabel = new JLabel(imageIcon);

        // Create a label with the message
        JLabel messageLabel = new JLabel("Close this window to start work");
        messageLabel.setHorizontalAlignment(JLabel.CENTER); // Center the text

        // Create and display the dialog
        JDialog imageDialog = new JDialog(frame, "Ready to Start!", true);
        imageDialog.setLayout(new BorderLayout());
        imageDialog.add(imageLabel, BorderLayout.CENTER);
        imageDialog.add(messageLabel, BorderLayout.SOUTH);
        imageDialog.pack();
        imageDialog.setLocationRelativeTo(frame);
        imageDialog.setVisible(true);
    }
    /*
     * MODIFIES: this
     * EFFECTS: Marks a selected task as completed and updates the task list model accordingly.
     */
//    private void markTaskAsCompleted() {
//        int selectedIndex = taskListView.getSelectedIndex();
//        if (selectedIndex != -1) {
//            String taskName = taskListModel.get(selectedIndex);
//            taskName = taskName.replace(" (uncompleted)", "") + " (completed)";
//            taskListModel.set(selectedIndex, taskName);
//
//
//            Task selectedTask = taskList.get(selectedIndex);
//            selectedTask.markIfCompleted();
//            statistics.addCompletedTaskList(selectedTask);
//            taskList.remove(selectedTask);
//        }
//    }

    /*
     * MODIFIES: this
     * EFFECTS: Marks the selected task as completed. Updates the task list model to reflect this change.
     *          If no task is selected or the selected task is not in the task list, shows an error message.
     *          Removes the completed task from the task list and adds it to the completed tasks in statistics.
     */
    private void markTaskAsCompleted() {
        int selectedIndex = taskListView.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= taskListModel.size()) {
            JOptionPane.showMessageDialog(frame, "Invalid task selection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String taskNameWithStatus = taskListModel.get(selectedIndex);
        // Get the pure task name without "(uncompleted)" or "(completed)".
        String taskName = taskNameWithStatus.replace(" (uncompleted)", "").replace(" (completed)", "");

        // Search for tasks in taskList
        for (Task task : taskList) {
            if (task.getTaskName().equals(taskName) && !task.isCompleted()) {
                task.markIfCompleted();
                statistics.addCompletedTaskList(task);
                taskListModel.set(selectedIndex, taskName + " (completed)");
                taskList.remove(task);
                return;
            }
        }

        // Error handling when a task is not found
        JOptionPane.showMessageDialog(frame, "Task not found in the list.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Starts the application by setting up the initial settings and starting the work session.
     *          Then prompts the user for further actions.
     */
    private void runApp() {
        firstSetting();
        showFunImage();
        start();

//        while (keepGoing) {
//            displayMenu();
//            int command = input.nextInt();
//            if (0 <= command && command <= 2) {
//                processCommand(command);
//            } else if (3 <= command && command <= 4) {
//                options1(command);
//            } else if (5 <= command && command <= 6) {
//                options2(command);
//            }
//        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Starts the work session.
     */
    private void start() {
        session.startWork();
        startSessionMonitor();
    }


    /*
     * MODIFIES: this
     * EFFECTS: Displays a menu to the user and prompts them to choose an option.
     *          The user can reset the timer, stop the timer, view statistics, or finish a task.
     */
    public void displayMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\t0 -> add task");
        System.out.println("\t1 -> save pomodoro session to file");
        System.out.println("\t2 -> load pomodoro session from file");
        System.out.println("\tIf you want to reset the timer, press 3");
        System.out.println("\tIf you want to stop the timer, press 4");
        System.out.println("\tIf you want to see the statistic of your work, press 5");
        System.out.println("\tIf you want to finish one task, press 6 to erase it");
    }

    /*
     * MODIFIES: this
     * EFFECTS: Processes user command related to task management and session persistence.
     */
    private void processCommand(int command) {
        if (command == 0) {
            addTask();
        } else if (command == 1) {
            savePomodoroSession();
        } else if (command == 2) {
            loadPomodoroSession();
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Saves the current pomodoro session to a file.
     */
    private void savePomodoroSession() {
        try {
            jsonWriter.open();
            jsonWriter.write(session, taskList);
            jsonWriter.close();
            System.out.println("Saved everything to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_STORE);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Loads a pomodoro session from a file.
     */
    private void loadPomodoroSession() {
        try {
            session = jsonReader.readPomodoroSession();
            statistics = session.getStatistics(); // Set the latest Statistics
            taskList = jsonReader.readTasks();

            // Update task list model
            taskListModel.clear();
            for (Task task : taskList) {
                String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
                taskListModel.addElement(task.getTaskName() + taskStatus);
            }

            //Resume session state
            if (session.isRunning()) {
                session.startTimer();
            }

            JOptionPane.showMessageDialog(frame, "Session loaded successfully.",
                    "Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to read from file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Allows the user to add a new task.
     */
    private void addTask() {
//        System.out.println("Please enter the name of the task: ");
//        String taskName = input.next();
//        Task newTask = new Task(taskName);
//        taskList.add(newTask);
//        show(taskList.size());

        String taskName = taskField.getText();
        if (!taskName.trim().isEmpty()) {
            taskListModel.addElement(taskName + " (uncompleted)"); // add task to taskListModel
            Task newTask = new Task(taskName); //from above
            taskList.add(newTask); //from above
            taskField.setText(""); // clear text field
        }
    }

    /*
     * REQUIRES: workDuration, shortBreak, and longBreak are non-negative integers
     * MODIFIES: this
     * EFFECTS: Sets up the initial settings for the Pomodoro session.
     *          Asks the user for work duration, short break duration, long break duration, and tasks.
     */
    public void firstSetting() {
//        System.out.println("How long do you want to work in one session: ");
//        int workDuration = input.nextInt();
//        input.nextLine();
//        System.out.println("How long do you need for a short break: ");
//        int shortBreak = input.nextInt();
//        input.nextLine();
//        System.out.println("How long do you need for a long break: ");
//        int longBreak = input.nextInt();
//        input.nextLine();
//        statistics = new Statistics();
//        collectTasks();
//        session = new PomodoroSession(workDuration, shortBreak, longBreak, statistics);   //all information
        showSettingsAndTaskDialog();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Opens a dialog box for the user to enter settings for the Pomodoro session and to add tasks.
     *          Validates input and updates session settings and task list based on user input.
     */
    private void showSettingsAndTaskDialog() {
        JDialog settingsDialog = new JDialog(frame, "Settings and Tasks", true);
        settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));
        settingsDialog.setSize(400, 300);

        JTextField[] fields = initializeSettingsFields(settingsDialog);
        JTextField workField = fields[0];
        JTextField shortBreakField = fields[1];
        JTextField longBreakField = fields[2];

        initializeTaskInputComponents2(settingsDialog);
        initializeConfirmSettingsButton(settingsDialog, workField, shortBreakField, longBreakField);

        updateTimerLabel(); // Update timer label
        settingsDialog.setVisible(true);
    }

    /*
     * EFFECTS: Creates and adds text fields for entering work, short break,
     *          and long break durations to the settings dialog.
     *          Returns an array of JTextField objects for these settings.
     */
    private JTextField[] initializeSettingsFields(JDialog settingsDialog) {
        // Text field for configuration
        JTextField workField = new JTextField(5);
        JTextField shortBreakField = new JTextField(5);
        JTextField longBreakField = new JTextField(5);

        settingsDialog.add(new JLabel("Work Duration (minutes):"));
        settingsDialog.add(workField);

        settingsDialog.add(new JLabel("Short Break Duration (minutes):"));
        settingsDialog.add(shortBreakField);

        settingsDialog.add(new JLabel("Long Break Duration (minutes):"));
        settingsDialog.add(longBreakField);

        return new JTextField[]{workField, shortBreakField, longBreakField};
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes and adds a confirm settings button to the settings dialog.
     *          Sets up the action listener to update session settings based on input values in the text fields.
     */
    private void initializeConfirmSettingsButton(JDialog settingsDialog, JTextField workField,
                                                 JTextField shortBreakField, JTextField longBreakField) {
        // Set button
        JButton confirmButton = new JButton("Confirm Settings");
        settingsDialog.add(confirmButton);
        confirmButton.addActionListener(e -> {
            try {
                int workDuration = Integer.parseInt(workField.getText());
                int shortBreak = Integer.parseInt(shortBreakField.getText());
                int longBreak = Integer.parseInt(longBreakField.getText());
                statistics = new Statistics();
                session = new PomodoroSession(workDuration, shortBreak, longBreak, statistics);
                settingsDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(settingsDialog, "Please enter valid numbers",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes components for task input within the settings dialog,
     *          including a text field for task names and a button to add tasks.
     *          Adds these components to the settings dialog.
     */
    private void initializeTaskInputComponents2(JDialog settingsDialog) {
        // Initialize text fields and buttons for tasks
        JTextField taskNameField = new JTextField(20);
        JButton addTaskButton = new JButton("Add Task");

        settingsDialog.add(new JLabel("Task Name:"));
        settingsDialog.add(taskNameField);
        settingsDialog.add(addTaskButton);

        // Use existing taskListModel
        JList<String> taskListView = new JList<>(taskListModel);
        settingsDialog.add(new JScrollPane(taskListView));

        // Action listener for the Add Task button
        addTaskButton.addActionListener(e -> {
            String taskName = taskNameField.getText().trim();
            if (!taskName.isEmpty()) {
                taskListModel.addElement(taskName + " (uncompleted)");
                taskList.add(new Task(taskName));
                taskNameField.setText(""); // Clear text field
            }
        });
    }


    /*
     * MODIFIES: this
     * EFFECTS: Collects tasks from the user.
     * Prompts the user to input the number of tasks and their names.
     */
//    public void collectTasks() {
//        System.out.println("How many tasks do you want to finish?: ");
//        taskList = new ArrayList<>();
//        int num = input.nextInt();
//        input.nextLine();
//        for (int i = 0; i < num; i++) {
//            System.out.println("What's the name of task?: ");
//            String name = input.nextLine();
//            Task task = new Task(name);
//            taskList.add(task);
//        }
//        show(num);
//    }

    /*
     * EFFECTS: Displays the names of the tasks.
     */
    public void show(int num) {
        for (int i = 0; i < num; i++) {
            Task taskShow = taskList.get(i);
            System.out.println(i + ": " + taskShow.getTaskName());
        }
    }

    /*
     * REQUIRES: command is an integer between 1 and 2
     * MODIFIES: this
     * EFFECTS: Handles the user's chosen command from the menu.
     *          Depending on the command, it will reset the timer, stop the timer.
     */
    public void options1(int command) {
        switch (command) {
            case 3:
                System.out.println("Reset it!!");
                session.resetTimer();
//                keepGoing = false;
                againForReset();
                break;
            case 4:
                stop();
                System.out.println("Great Work!!");
                again();
                break;
        }
    }

    /*
     * MODIFIES: this.session, this.sessionMonitorTimer
     * EFFECTS: Stops the current pomodoro session and cancels the session monitoring timer.
     */
    public void stop() {
        if (session != null) {
            session.stop();
        }
        if (sessionMonitorTimer != null) {
            sessionMonitorTimer.cancel();
        }
    }

    /*
     * EFFECTS: Asks the user if they want to resume or leave.
     */
    public void againForReset() {
        System.out.println("If you want to resume, press 1");
        System.out.println("If you want to leave, press 2");
        int num = input.nextInt();
        switch (num) {
            case 1:
                keepGoing = true;
                session.startWork();
                show(taskList.size());
                break;
            case 2:
                System.out.println("See you soon");
                keepGoing = false;
                break;
        }
    }

    /*
     * EFFECTS: Asks the user if they want to resume or leave.
     */
    public void again() {
        System.out.println("If you want to resume, press 1");
        System.out.println("If you want to leave, press 2");
        int num = input.nextInt();
        switch (num) {
            case 1:
                keepGoing = true;
                session.startTimer();
                show(taskList.size());
                break;
            case 2:
                System.out.println("See you soon");
                keepGoing = false;
                break;
        }
    }

    /*
     * REQUIRES: command is an integer between 3 and 4
     * MODIFIES: this
     * EFFECTS: Handles the user's chosen command from the menu.
     *          Depending on the command, it will display statistics, or mark a task as completed.
     */
    public void options2(int command) {
        switch (command) {
            case 5:
                System.out.println("the number of finished session: " + statistics.getCompletedSessions());
                System.out.println("the length of working time is : " + statistics.getTotalWorkTime() + "seconds");
                break;
            case 6:
                case6();
                break;
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Allows the user to mark a task as completed.
     */
    public void case6() {
//        if (taskList.isEmpty()) {
//            System.out.println("There is nothing to do anymore");
//            System.out.println("Nice work");
//        } else {
//            System.out.println("Which task? From the top, what number is it(from 0)?");
//            int index = input.nextInt();
//            Task finishedTask = taskList.get(index);  //!!
//            finishedTask.markIfCompleted();
//
//            int size = taskList.size();
//            statistics.addCompletedTaskList(finishedTask);
//            taskList.remove(finishedTask);
//            System.out.println("Good job");
//            show(taskList.size());
//        }
        int selectedIndex = taskListView.getSelectedIndex();
        if (selectedIndex != -1) {
            String taskName = taskListModel.get(selectedIndex).replace(" (uncompleted)", "");
            // Searches for the corresponding Task object in the task list and marks it as complete
            for (Task task : taskList) {
                if (task.getTaskName().equals(taskName)) {
                    task.markIfCompleted();
                    break;
                }
            }
            taskListModel.set(selectedIndex, taskName + " (completed)"); // Update listing model
        }
    }

    /*
     * EFFECTS: Displays statistics of the pomodoro sessions if available. Shows a message dialog with the number of
     * completed sessions and total work time in seconds. If no statistics are available, shows a warning message.
     */
    private void showStatistics() {
        if (statistics != null) {
            String statsText = "Completed Sessions: " + statistics.getCompletedSessions() + "\n" + "Total Work Time: "
                    + statistics.getTotalWorkTime() + " seconds";
            JOptionPane.showMessageDialog(frame, statsText, "Statistics", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "No statistics available.", "Statistics", JOptionPane.WARNING_MESSAGE);
        }
    }

    /*
     * REQUIRES: session must be a valid PomodoroSession instance
     * MODIFIES: this
     * EFFECTS: Starts a timer task that checks every second if a break has started or ended within the session.
     * If a break has started, shows an information message dialog. If a break has ended, shows an information message
     * dialog to resume work. Updates the wasOnBreak flag according to the current session state.
     */
    private void startSessionMonitor() {
        sessionMonitorTimer = new Timer();
        sessionMonitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session != null) {
                    if (session.isOnBreak() && !wasOnBreak) {
                        // When the break began
                        JOptionPane.showMessageDialog(frame, "A break has begun!",
                                "Break begins", JOptionPane.INFORMATION_MESSAGE);
                    } else if (!session.isOnBreak() && wasOnBreak) {
                        // When the break is over
                        JOptionPane.showMessageDialog(frame, "Break is over. Let's resume work!",
                                "End of break", JOptionPane.INFORMATION_MESSAGE);
                    }
                    wasOnBreak = session.isOnBreak();
                }
            }
        }, 0, 1000); //check every second
    }

    // Add to the end processing part of the application
    public void onApplicationExit() {
        // Output event log
        for (Event e : EventLog.getInstance()) {
            System.out.println(e);
        }
    }
}