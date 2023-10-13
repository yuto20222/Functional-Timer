package ui;

import model.PomodoroSession;
import model.Statistics;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PomodoroApp {
    private PomodoroSession session;
    private Statistics statistics;
    private Task task;
    private List<Task> taskList;

    private Scanner input;

    public PomodoroApp() {
        input = new Scanner(System.in);
        runApp();
    }

    private void runApp() {
        firstSetting();
        session.startWork();
        choose();
    }

    public void choose() {
        System.out.println("If you want to reset the timer, press 1");
        System.out.println("If you want to stop the timer, press 2");
        System.out.println("If you want to see the statistic of your work, press 3");
        System.out.println("If you want to finish one task, press 4 to erase it");
        int command = input.nextInt();
        options(command);
    }

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
        System.out.println("How many tasks do you want to finish?: ");
        taskList = new ArrayList<>();
        int num = input.nextInt();
        input.nextLine();
        for (int i = 0; i < num; i++) {
            System.out.println("What's the name of task?: ");
            String name = input.nextLine();
            task = new Task(name);
            taskList.add(task);
        }
        session = new PomodoroSession(workDuration, shortBreak, longBreak, statistics);
    }

    public void options(int command) {
        switch (command) {
            case 1:
                session.resetTimer();
                System.out.println("when you are ready, press 1");
                if (input.nextInt() == 1) {
                    session.startWork();
                }
            case 2:
                session.stop();
                System.out.println("Great Work!!");
                break;
            case 3:
                System.out.println("the number of finished session: " + statistics.getCompletedSessions());
                System.out.println("the length of working time is : " + statistics.getTotalWorkTime());
                choose();
            case 4:
                System.out.println("Which task? From the top, what number is it?");
                int index = input.nextInt();
                Task finishedTask = taskList.get(index);
                finishedTask.isCompleted();
                System.out.println("Good job");
                choose();
        }
    }
}
