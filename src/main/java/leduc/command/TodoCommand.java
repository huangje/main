package leduc.command;

import leduc.exception.EmptyTodoException;
import leduc.exception.FileException;
import leduc.exception.PrioritizeLimitException;
import leduc.storage.Storage;
import leduc.ui.Ui;
import leduc.task.TaskList;
import leduc.task.TodoTask;


/**
 * Represents a Todo Command.
 * Allows to add the todo task in the tasks list and the data file.
 */
public class TodoCommand extends Command {

    /**
     * static variable used for shortcut
     */
    private static String todoShortcut = "todo";
    /**
     * Constructor of TodoCommand.
     * @param userInput String which represent the input string of the user.
     */
    public  TodoCommand(String userInput){
        super(userInput);
    }

    /**
     * Allow to add the task in the tasks list and the data file.
     * @param tasks leduc.task.TaskList which is the list of task.
     * @param ui leduc.ui.Ui which deals with the interactions with the user.
     * @param storage leduc.storage.Storage which deals with loading tasks from the file and saving tasks in the file.
     * @throws EmptyTodoException Exception caught when the description of the todo list is not given by the user.
     * @throws FileException Exception caught when the file can't be open or read or modify
     * @throws PrioritizeLimitException Exception caught when the new priority is greater than 9 or less than 0.
     */
    public void execute(TaskList tasks, Ui ui, Storage storage) throws EmptyTodoException, FileException, PrioritizeLimitException {
        String userSubstring;
        if(isCalledByShortcut){
            userSubstring = userInput.substring(TodoCommand.todoShortcut.length());
        }
        else {
            userSubstring = userInput.substring(4);
        }
        String[] prioritySplit = userSubstring.split("prio");
        TodoTask newTask = null;
        int priority = -1 ;
        if (prioritySplit.length != 1 && prioritySplit[1].trim().matches("\\d+")){
            priority = Integer.parseInt(prioritySplit[1].trim());
            if (priority < 1 || priority > 9) {
                throw new PrioritizeLimitException();
            }
            if (prioritySplit[0].isBlank()){
                throw new EmptyTodoException();
            }
            newTask = new TodoTask(prioritySplit[0],priority );
        }
        else { // The description of the todo task could contain "prio"
            // For exmaple, "todo prio" create a todo task with description prio and priority 5
            if (userSubstring.isBlank()){
                throw new EmptyTodoException();
            }
            newTask = new TodoTask(userSubstring.trim());
        }
        tasks.add(newTask);
        storage.save(tasks.getList());
        ui.showTask(newTask, tasks.size());
    }
    /**
     * getter because the shortcut is private
     * @return the shortcut name
     */
    public static String getTodoShortcut() {
        return todoShortcut;
    }

    /**
     * used when the user want to change the shortcut
     * @param todoShortcut the new shortcut
     */
    public static void setTodoShortcut(String todoShortcut) {
        TodoCommand.todoShortcut = todoShortcut;
    }
}
