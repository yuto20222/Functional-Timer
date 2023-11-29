# **My Personal Project**
## pomodoro timer

*What will the application do?*
- The Pomodoro Timer application is a time management tool that helps users boost productivity and focus by breaking their work or study sessions into intervals, traditionally 25 minutes in length, separated by short breaks.
  - Timer Control: Allow users to start and stop the Pomodoro timer.
  - Customizable Settings: Enable users to customize timer durations for work intervals, short breaks, and long breaks.
  - Notifications: Provide notifications or alerts when each timer interval ends.
  - Task Management: Allow users to associate tasks with each Pomodoro session, helping them track their progress.
  - Statistics: Maintain statistics on completed Pomodoro sessions, including the number of sessions completed, total work time.

*Who will use it?*
- The Pomodoro Timer application can be used by a wide range of individuals who want to improve their time management and productivity

*Why is this project of interest to you?*
- I want to have my own pomodoro timer since I love this tool.
- Since I am used to use it, it might be easier than other projects.

## UserStory
- As a user, I want to be able to add completed study session to the statics:
- As a user, I want to be able to view the list of completed study session in my statics:
- As a user, I want to be able to reset a Pomodoro Timer(just Timer): // difficult
- As a user, I want to be able to pause and resume a Pomodoro session:
- As a user, I want to be able to add a new task to my Pomodoro session(List of Tasks):
- As a user, I want to be able to change task's status from "Uncompleted" to "Completed"(in List of Tasks):

- As a user, I want to save my current timer condition, statistics, and finished tasks to a file.
- As a user, I want to reload my saved timer condition, statistics, and finished tasks from a file.

# Instructions for Grader
- You can generate the first required action related to the user story "adding New Tasks to Pomodoro session(List of Task)" by clicking the button labelled "Add Task"
- You can generate the second required action related to the user story "change task's status from "Uncompleted" to "Completed"(in List of Tasks):" by pressing the button labelled "Mark As Completed" while selecting a task().
- You can save the state of my application by clicking "Save Session" button
- You can reload the state of my application clicking "Load Session" button
- You can put however many tasks you want and set each session time(Work Duration, Short Break Duration, and Long Break Duration) on the first window with the button labelled "Confirm Settings".
- You can check how many work session you have finished so far by clicking the button labelled "View Statistics"
- You can reset just Timer by clicking the button labelled "Reset Timer" 
- You can stop Timer by clicking the button labelled "Stop Timer" 
- You can find the visual component after clicking the button labelled "Confirm Setting"

# Phase 4: Task 2
- Mon Nov 27 19:20:24 PST 2023
  Uncompleted Task is added: uncompleted
  Mon Nov 27 19:20:41 PST 2023
  Uncompleted Task is added: uncompletedButChangedtoCompleted
  Mon Nov 27 19:20:42 PST 2023
  Uncompleted Task is completed: uncompletedButChangedtoCompleted
  Mon Nov 27 19:20:42 PST 2023
  Completed Task added to the stat: uncompletedButChangedtoCompleted

# Phase 4: Task 3
- In the next update of our Pomodoro Timer app, I'm planning to make some big changes to the code. The main goal is to make the code easier to understand and work with, especially as we add more features in the future.
- Firstly, I noticed that the PomodoroApp class is doing too much - it handles both the user interface (UI) and the app's main functions. To fix this, I'm going to split it into two classes. A new class, PomodoroAppUI, will take care of all the UI stuff, like buttons and text fields. This will make the original PomodoroApp class simpler and just focused on the app's main tasks. It's like separating the kitchen and living room in a house, so each has its own clear space. Another thing I want to change is how tasks are managed in the PomodoroApp class. Right now, this class also deals with tasks, like adding and completing them. I plan to create a TaskManager class that will handle all these task-related jobs. This way, the PomodoroApp class won't be overloaded with too many responsibilities. Also, I'm thinking of making a new StatisticsController class. At the moment, PomodoroApp directly uses the Statistics class for tracking things like how many sessions you've completed. With StatisticsController, all the statistics stuff will be in one place, making it easier to handle and change if needed. By doing these changes, I'm hoping to make our code cleaner and more organized. This should help us a lot when we want to add new features or fix bugs in the future.

