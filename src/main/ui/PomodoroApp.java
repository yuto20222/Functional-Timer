package ui;

import javax.swing.*;
import java.awt.*;
import model.PomodoroSession;
import model.Statistics;
import model.Task;
import persistence.JsonReader;
import persistence.JsonWriter;

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
    private Statistics statistics;
    private List<Task> taskList;
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
        frame.setLayout(new FlowLayout()); // レイアウトの設定

        initializeTaskInputComponents();
        initializeTaskListView();
        initializeStatisticsButton();
        initializeSaveButton();
        initializeLoadButton();

        // タイマー関連のコンポーネントを初期化
        initializeTimerLabel();
        initializeResetTimerButton();
        initializeStopTimerButton();

        frame.setVisible(true); // GUIを表示
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
                    = String.format("Current settings：\nWorking time：%d sec\nShort Break：%d sec\nLong Break：%d sec",
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
        }, 0, 1000); // 毎秒更新
    }

    private void initializeTaskInputComponents() {
        // タスク入力用テキストフィールドとボタンの初期化
        // タスク入力用テキストフィールド
        taskField = new JTextField(20);
        frame.add(taskField);

        // タスク追加ボタン
        addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> addTask());
        frame.add(addTaskButton);
    }

    private void initializeTaskListView() {
        // タスクリストビューの初期化
        // タスクリストモデルとビュー
        taskListModel = new DefaultListModel<>();
        taskListView = new JList<>(taskListModel);
        taskListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(taskListView);
        listScrollPane.setPreferredSize(new Dimension(250, 150));
        frame.add(listScrollPane);

        // 完了マークボタン
        markCompletedButton = new JButton("Mark as Completed");
        markCompletedButton.addActionListener(e -> markTaskAsCompleted());
        frame.add(markCompletedButton);
    }

    private void initializeStatisticsButton() {
        // 統計表示ボタンの初期化
        //statボタン
        JButton statsButton = new JButton("View Statistics");
        statsButton.addActionListener(e -> showStatistics());
        frame.add(statsButton);
    }

    private void initializeSaveButton() {
        // セーブボタン
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

    private void initializeLoadButton() {
        // ロードボタンの初期化
        loadButton = new JButton("Load Session");
        loadButton.addActionListener(e -> loadSession());
        frame.add(loadButton);
    }

    private void loadSession() {
        // ロード処理の実行
        try {
            session = jsonReader.readPomodoroSession();
            statistics = session.getStatistics(); // 最新のStatisticsを設定
            taskList = jsonReader.readTasks();
            updateTaskListModel();
            if (session.isRunning()) {
                session.startTimer();
                updateTimerLabelImmediately(); // タイマーラベルを即座に更新するメソッドを追加
            }
            JOptionPane.showMessageDialog(frame, "Session loaded successfully.",
                    "Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to read from file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTaskListModel() {
        // タスクリストモデルを更新
        taskListModel.clear(); // まず既存のリストをクリアする
        List<Task> completedTasks = statistics.getCompletedTaskList();
        for (Task task : completedTasks) {
            String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
            taskListModel.addElement(task.getTaskName() + taskStatus); // 新しいタスクを追加
        }
        for (Task task : taskList) {
            String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
            taskListModel.addElement(task.getTaskName() + taskStatus); // 新しいタスクを追加
        }
    }

    private void initializeTimerLabel() {
        // タイマーラベルの初期化
        timerLabel = new JLabel("00:00");
        frame.add(timerLabel);
    }

    private void initializeResetTimerButton() {
        // リセットタイマーボタンの初期化
        JButton resetTimerButton = new JButton("Reset Timer");
        resetTimerButton.addActionListener(e -> resetTimer());
        frame.add(resetTimerButton);
    }

    private void resetTimer() {
        if (session == null) {
            return; // 早期リターン
        }

        session.resetTimer();
        cancelExistingTimer(); // タイマーのキャンセルを別のメソッドに分離

        int result = JOptionPane.showOptionDialog(frame, "The timer has been reset. What should I do?",
                "Reset Timer", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"continue", "end"}, "continue");

        if (result == JOptionPane.YES_OPTION) {
            continueAfterReset();
        } else {
            System.exit(0);
        }
    }

    private void cancelExistingTimer() {
        if (sessionMonitorTimer != null) {
            sessionMonitorTimer.cancel();
        }
    }

    private void continueAfterReset() {
        session.startWork();
        updateTimerLabel();
        startSessionMonitor();
        showCurrentSettings();
    }

    private void initializeStopTimerButton() {
        // ストップタイマーボタンの初期化
        JButton stopTimerButton = new JButton("Stop Timer");
        stopTimerButton.addActionListener(e -> stopTimer());
        frame.add(stopTimerButton);
    }

    private void stopTimer() {
        // ストップタイマーボタンのアクション
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
                // 再開の場合
                keepGoing = true;
                session.startTimer();
                updateTimerLabel();
                startSessionMonitor();
                show(taskList.size());
            } else {
                // 退出の場合
                System.exit(0);
            }
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Marks a selected task as completed and updates the task list model accordingly.
     */
    private void markTaskAsCompleted() {
        int selectedIndex = taskListView.getSelectedIndex();
        if (selectedIndex != -1) {
            String taskName = taskListModel.get(selectedIndex);
            // タスク名から "(uncompleted)" を削除し、"(completed)" を追加
            taskName = taskName.replace(" (uncompleted)", "") + " (completed)";
            taskListModel.set(selectedIndex, taskName);

            // 必要に応じて、内部の taskList でのタスク状態も更新q
            Task selectedTask = taskList.get(selectedIndex);
            selectedTask.markIfCompleted();
            statistics.addCompletedTaskList(selectedTask);
            taskList.remove(selectedTask);
        }
    }


    /*
     * MODIFIES: this
     * EFFECTS: Starts the application by setting up the initial settings and starting the work session.
     *          Then prompts the user for further actions.
     */
    private void runApp() {
        firstSetting();
        start();

        while (keepGoing) {
            displayMenu();
            int command = input.nextInt();
            if (0 <= command && command <= 2) {
                processCommand(command);
            } else if (3 <= command && command <= 4) {
                options1(command);
            } else if (5 <= command && command <= 6) {
                options2(command);
            }
        }
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
            statistics = session.getStatistics(); // 最新のStatisticsを設定
            taskList = jsonReader.readTasks();

            // タスクリストモデルを更新
            taskListModel.clear();
            for (Task task : taskList) {
                String taskStatus = task.isCompleted() ? " (completed)" : " (uncompleted)";
                taskListModel.addElement(task.getTaskName() + taskStatus);
            }

            // セッションの状態を再開
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
            taskListModel.addElement(taskName + " (uncompleted)"); // リストモデルにタスクを追加
            Task newTask = new Task(taskName); //from above
            taskList.add(newTask); //from above
            taskField.setText(""); // テキストフィールドをクリア
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
     * EFFECTS: Opens a dialog for the user to enter the settings for work duration,
     *          short break, and long break durations,
     *          as well as to add new tasks.
     *          Updates session settings and task list based on user input.
     *          Validates numerical input
     *          and handles incorrect formats with an error message.
     */
    private void showSettingsAndTaskDialog() {
        JDialog settingsDialog = new JDialog(frame, "Settings and Tasks", true);
        settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));
        settingsDialog.setSize(400, 300);

        // 設定のためのテキストフィールド
        JTextField workField = new JTextField(5);
        settingsDialog.add(new JLabel("Work Duration (minutes):"));
        settingsDialog.add(workField);

        JTextField shortBreakField = new JTextField(5);
        settingsDialog.add(new JLabel("Short Break Duration (minutes):"));
        settingsDialog.add(shortBreakField);

        JTextField longBreakField = new JTextField(5);
        settingsDialog.add(new JLabel("Long Break Duration (minutes):"));
        settingsDialog.add(longBreakField);

        // タスクのためのテキストフィールドとボタン
        JTextField taskNameField = new JTextField(20);
        settingsDialog.add(new JLabel("Task Name:"));
        settingsDialog.add(taskNameField);
        JButton addTaskButton = new JButton("Add Task");
        settingsDialog.add(addTaskButton);

        // 既存の taskListModel を使用
        JList<String> taskListView = new JList<>(taskListModel);
        settingsDialog.add(new JScrollPane(taskListView));

        addTaskButton.addActionListener(e -> {
            taskList = new ArrayList<>(); //追加した
            String taskName = taskNameField.getText().trim();
            if (!taskName.isEmpty()) {
                taskListModel.addElement(taskName + " (uncompleted)"); // 既存のリストモデルにタスクを追加
                taskNameField.setText("");
                taskList.add(new Task(taskName)); // タスクリストに追加
            }
        });

        // 設定確定ボタン
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

        updateTimerLabel(); // タイマーラベルを更新
        settingsDialog.setVisible(true);
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
            // タスクリスト内の対応するTaskオブジェクトを検索し、それを完了としてマーク
            for (Task task : taskList) {
                if (task.getTaskName().equals(taskName)) {
                    task.markIfCompleted();
                    break;
                }
            }
            taskListModel.set(selectedIndex, taskName + " (completed)"); // リストモデルを更新
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
                        // 休憩が始まったとき
                        JOptionPane.showMessageDialog(frame, "A break has begun!",
                                "Break begins", JOptionPane.INFORMATION_MESSAGE);
                    } else if (!session.isOnBreak() && wasOnBreak) {
                        // 休憩が終わったとき
                        JOptionPane.showMessageDialog(frame, "Break is over. Let's resume work!",
                                "End of break", JOptionPane.INFORMATION_MESSAGE);
                    }
                    wasOnBreak = session.isOnBreak();
                }
            }
        }, 0, 1000); // 例えば、毎秒チェックする
    }
}
