/* @@author rshah918 */
package leduc.command;
import leduc.ui.Ui;
import leduc.storage.Storage;
import leduc.task.Task;
import leduc.task.TaskList;
import java.util.ArrayList;
/**
 * Will display all unfinished tasks in the tasklist
 */
public class UnfinishedCommand extends Command {
    /**
     * static variable used for shortcut
     */
    private static String unfinishedShortcut = "unfinished";
    /**
    /**
     * Constructor of UnfinishedCommand.
     * @param userInput String which represent the input string of the user.
     */
    public UnfinishedCommand(String userInput){
        super(userInput);
    }
    /**
     * Allows users to see all unfinished tasks.
     * @param tasks leduc.task.TaskList which is the list of task.
     * @param ui leduc.ui.Ui which deals with the interactions with the user.
     * @param storage leduc.storage.Storage which deals with loading tasks from the file and saving tasks in the file.
     */
    public void execute(TaskList tasks, Ui ui, Storage storage){
        ArrayList<Task> unfinishedTaskList = new ArrayList<Task>();
        //Iterate through the tasklist
        for(int i = 0; i < tasks.size(); i++){
            Task task = tasks.get(i);
            String mark = task.getMark();
            //check if the task is unfinished
            if(mark.equals("[X]")) {
                unfinishedTaskList.add(task);
            }
        }
        //call ui to display output
        ui.showUnFinishedTasks(unfinishedTaskList);
    }
/* @@author */
    /**
     * getter because the shortcut is private
     * @return the shortcut name
     */
    public static String getUnfinishedShortcut() {
        return unfinishedShortcut;
    }
    /**
     * used when the user want to change the shortcut
     * @param unfinishedShortcut the new shortcut
     */
    public static void setUnfinishedShortcut(String unfinishedShortcut) {
        UnfinishedCommand.unfinishedShortcut = unfinishedShortcut;
    }
}