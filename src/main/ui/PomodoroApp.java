package ui;

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

// Represents the user interface for the Pomodoro application.
// Allows users to set up work sessions, take breaks, and manage tasks.
public class PomodoroApp {
    private PomodoroSession session;
    private Statistics statistics;
    private List<Task> taskList;
    private final Scanner input;
    boolean keepGoing = true;
    private static final String JSON_STORE = "./data/pomodoro.json";
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the input scanner and runs the application.
     */
    public PomodoroApp() {
        input = new Scanner(System.in);
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        runApp();
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
            } else if (4 <= command && command <= 5) {
                options1(command);
            } else if (6 <= command && command <= 7) {
                options2(command);
            }
        }
    }

    private void start() {
        session.startWork();
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
//        System.out.println("\t3 -> quit");
        System.out.println("\tIf you want to reset the timer, press 4");
        System.out.println("\tIf you want to stop the timer, press 5");
        System.out.println("\tIf you want to see the statistic of your work, press 6");
        System.out.println("\tIf you want to finish one task, press 7 to erase it");
    }

    private void processCommand(int command) {
        if (command == 0) {
            addTask();
        } else if (command == 1) {
            savePomodoroSession();
        } else if (command == 2) {
            loadPomodoroSession();
        }
    }

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

    private void loadPomodoroSession() {
        try {
            session = jsonReader.readPomodoroSession();
//            statistics = jsonReader.readStatistics();
            taskList = jsonReader.readTasks();
            System.out.println("Loaded pomodoro session from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
        }
    }

    private void addTask() {
        System.out.println("Please enter the name of the task: ");
        String taskName = input.next();
        Task newTask = new Task(taskName);
        taskList.add(newTask);
        show(taskList.size());
    }

    /*
     * REQUIRES: workDuration, shortBreak, and longBreak are non-negative integers
     * MODIFIES: this
     * EFFECTS: Sets up the initial settings for the Pomodoro session.
     *          Asks the user for work duration, short break duration, long break duration, and tasks.
     */
    public void firstSetting() {
        System.out.println("How long do you want to work in one session: ");
        int workDuration = input.nextInt();
        input.nextLine();
        System.out.println("How long do you need for a short break: ");
        int shortBreak = input.nextInt();
        input.nextLine();
        System.out.println("How long do you need for a long break: ");
        int longBreak = input.nextInt();
        input.nextLine();
        statistics = new Statistics();
        collectTasks();
        session = new PomodoroSession(workDuration, shortBreak, longBreak, statistics);   //all information
    }

    /*
     * REQUIRES: The number of tasks entered by the user is a non-negative integer
     * MODIFIES: this
     * EFFECTS: Prompts the user to input the number of tasks and their names.
     *          Initializes and adds the tasks to the task list.
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
            case 4:
                System.out.println("If you want to use this again, please resume this again");
                session.resetTimer();
                keepGoing = false;
                break;
            case 5:
                session.stop();
                System.out.println("Great Work!!");
                again();
                break;
        }
    }

    public void again() {
        System.out.println("If you want to resume, press 1");
        System.out.println("If you want to leave, press 2");
        int num = input.nextInt();
        switch (num) {
            case 1:
                keepGoing = true;
                start();
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
            case 6:
                System.out.println("the number of finished session: " + statistics.getCompletedSessions());
                System.out.println("the length of working time is : " + statistics.getTotalWorkTime() + "seconds");
                break;
            case 7:
                case7();
                break;
        }
    }

    public void case7() {
        if (taskList.isEmpty()) {
            System.out.println("There is nothing to do anymore");
            System.out.println("Nice work");
        } else {
            System.out.println("Which task? From the top, what number is it(from 0)?");
            int index = input.nextInt();
            Task finishedTask = taskList.get(index);  //!!
            finishedTask.markIfCompleted();

            int size = taskList.size();
            statistics.addCompletedTaskList(finishedTask);
            taskList.remove(finishedTask);
            System.out.println("Good job");
            show(taskList.size());
        }
    }
}
