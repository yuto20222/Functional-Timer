package ui;

import model.PomodoroSession;
import model.Statistics;
import model.Task;

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

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the input scanner and runs the application.
     */
    public PomodoroApp() {
        input = new Scanner(System.in);
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
    }

    private void start() {
        session.startWork();
        choose();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Displays a menu to the user and prompts them to choose an option.
     *          The user can reset the timer, stop the timer, view statistics, or finish a task.
     */
    public void choose() {
        System.out.println("If you want to reset the timer, press 1");
        System.out.println("If you want to stop the timer, press 2");
        System.out.println("If you want to see the statistic of your work, press 3");
        System.out.println("If you want to finish one task, press 4 to erase it");
        int command = input.nextInt();
        if (command == 1 || command == 2) {
            options1(command);
        } else {
            options2(command);
        }
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
        session = new PomodoroSession(workDuration, shortBreak, longBreak, statistics);
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
            case 1:
                session.resetTimer();
                System.out.println("See you soon");
                break;
            case 2:
                session.stop();
                System.out.println("Great Work!!");
                again();
        }
    }

    public void again() {
        System.out.println("If you want to resume, press 1");
        System.out.println("If you want to leave, press 2");
        int num = input.nextInt();
        switch (num) {
            case 1:
                start();
                break;
            case 2:
                System.out.println("See you soon");
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
            case 3:
                System.out.println("the number of finished session: " + statistics.getCompletedSessions());
                System.out.println("the length of working time is : " + statistics.getTotalWorkTime() + "seconds");
                choose();
                break;
            case 4:
                empty();
                System.out.println("Which task? From the top, what number is it(from 0)?");
                int index = input.nextInt();
                Task finishedTask = taskList.get(index);
                finishedTask.markIfCompleted();
                for (int i = 0; i < taskList.size(); i++) {
                    if (finishedTask.isCompleted()) {
                        taskList.remove(finishedTask);
                    }
                }
                System.out.println("Good job");
                show(taskList.size());
                choose();
                break;
        }
    }

    public void empty() {
        if (taskList.isEmpty()) {
            System.out.println("There is nothing to do anymore");
            System.out.println("Nice work");
        }
    }
}
