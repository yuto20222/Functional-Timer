package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private void initializeUI() {
        frame = new JFrame("Pomodoro App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout()); // レイアウトの設定

        // タスク入力用テキストフィールド
        taskField = new JTextField(20);
        frame.add(taskField);

        // タスク追加ボタン
        addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        frame.add(addTaskButton);

        // タスクリストモデルとビュー
        taskListModel = new DefaultListModel<>();
        taskListView = new JList<>(taskListModel);
        taskListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(taskListView);
        listScrollPane.setPreferredSize(new Dimension(250, 150));
        frame.add(listScrollPane);

        // 完了マークボタン
        markCompletedButton = new JButton("Mark as Completed");
        markCompletedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markTaskAsCompleted();
            }
        });
        frame.add(markCompletedButton);

        //statボタン
        JButton statsButton = new JButton("View Statistics");
        statsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatistics();
            }
        });
        frame.add(statsButton);

        // セーブボタン
        saveButton = new JButton("Save Session");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePomodoroSession();
            }
        });
        frame.add(saveButton);

        // ロードボタン
        loadButton = new JButton("Load Session");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPomodoroSession();
            }
        });
        frame.add(loadButton);

        // タイマーラベル
        timerLabel = new JLabel("00:00");
        frame.add(timerLabel);

//        workField = new JTextField(5);
//        shortBreakField = new JTextField(5);
//        longBreakField = new JTextField(5);
//
//        // Add the components to the frame with labels
//        frame.add(new JLabel("Work Duration (minutes):"));
//        frame.add(workField);
//
//        frame.add(new JLabel("Short Break Duration (minutes):"));
//        frame.add(shortBreakField);
//
//        frame.add(new JLabel("Long Break Duration (minutes):"));
//        frame.add(longBreakField);

        JButton resetTimerButton = new JButton("Reset Timer");
        resetTimerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (session != null) {
                    session.resetTimer();
                }
            }
        });
        frame.add(resetTimerButton);

        JButton stopTimerButton = new JButton("Stop Timer");
        stopTimerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (session != null) {
                    session.stop();
                }
            }
        });
        frame.add(stopTimerButton);

        frame.setVisible(true); // GUIを表示
    }

    private void markTaskAsCompleted() {
        int selectedIndex = taskListView.getSelectedIndex();
        if (selectedIndex != -1) {
            String taskName = taskListModel.get(selectedIndex);
            // タスク名から "(uncompleted)" を削除し、"(completed)" を追加
            taskName = taskName.replace(" (uncompleted)", "") + " (completed)";
            taskListModel.set(selectedIndex, taskName);

            // 必要に応じて、内部の taskList でのタスク状態も更新
            Task selectedTask = taskList.get(selectedIndex);
            selectedTask.markIfCompleted();
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
            statistics = session.getStatistics(); // ここで最新のStatisticsを設定
            taskList = jsonReader.readTasks();

            // セッションの状態を再開
            if (session.isRunning()) {
                // もしセッションが実行中であれば、タイマーを再開する
                session.startTimer();
            }

        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
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

        addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskName = taskNameField.getText().trim();
                if (!taskName.isEmpty()) {
                    taskListModel.addElement(taskName + " (uncompleted)"); // 既存のリストモデルにタスクを追加
                    taskNameField.setText("");
                    taskList.add(new Task(taskName)); // タスクリストに追加
                }
            }
        });

        // 設定確定ボタン
        JButton confirmButton = new JButton("Confirm Settings");
        settingsDialog.add(confirmButton);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        settingsDialog.setVisible(true);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Collects tasks from the user.
     * Prompts the user to input the number of tasks and their names.
     */
    public void collectTasks() {
        System.out.println("How many tasks do you want to finish?: ");
        taskList = new ArrayList<>();
        int num = input.nextInt();
        input.nextLine();
        for (int i = 0; i < num; i++) {
            System.out.println("What's the name of task?: ");
            String name = input.nextLine();
            Task task = new Task(name);
            taskList.add(task);
        }
        show(num);
    }

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

    private void showStatistics() {
        if (statistics != null) {
            String statsText = "Completed Sessions: " + statistics.getCompletedSessions() + "\n" + "Total Work Time: "
                    + statistics.getTotalWorkTime() + " seconds";
            JOptionPane.showMessageDialog(frame, statsText, "Statistics", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "No statistics available.", "Statistics", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void startSessionMonitor() {
        sessionMonitorTimer = new Timer();
        sessionMonitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session != null) {
                    if (session.isOnBreak() && !wasOnBreak) {
                        // 休憩が始まったとき
                        JOptionPane.showMessageDialog(frame, "休憩が始まりました！", "休憩開始", JOptionPane.INFORMATION_MESSAGE);
                    } else if (!session.isOnBreak() && wasOnBreak) {
                        // 休憩が終わったとき
                        JOptionPane.showMessageDialog(frame, "休憩が終わりました。作業を再開しましょう！", "休憩終了", JOptionPane.INFORMATION_MESSAGE);
                    }
                    wasOnBreak = session.isOnBreak();
                }
            }
        }, 0, 1000); // 例えば、毎秒チェックする
    }
}
