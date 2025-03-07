package leduc.command;

import leduc.exception.FileException;
import leduc.exception.NonExistentTaskException;
import leduc.storage.Storage;
import leduc.ui.Ui;
import leduc.task.TaskList;

/**
 * Represents a Delete Command.
 */
public class DoneCommand extends Command {

    /**
     * static variable used for shortcut
     */
    private static String doneShortcut = "done";
    /**
     * Constructor of DoneCommand
     * @param userInput String which represent the input string of the user.
     */
    public  DoneCommand(String userInput){
        super(userInput);
    }

    /**
     * Change the mark of a task to done ("[V]").
     * @param tasks leduc.task.TaskList which is the list of task.
     * @param ui leduc.ui.Ui which deals with the interactions with the user.
     * @param storage leduc.storage.Storage which deals with loading tasks from the file and saving tasks in the file.
     * @throws NonExistentTaskException Exception caught when the task which is done does not exist.
     * @throws FileException Exception caught when the file can't be open or read or modify
     */
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NonExistentTaskException, FileException {
        String userSubstring;
        if(isCalledByShortcut){
            userSubstring = userInput.substring(DoneCommand.doneShortcut.length() + 1);
        }
        else {
            userSubstring = userInput.substring(5);
        }
        int index = Integer.parseInt(userSubstring) - 1;
        if (index > tasks.size() - 1 || index < 0) {
            throw new NonExistentTaskException();
        }
        else { // to change the mark, the whole file is rewritten ( probably a better way to do it)
            tasks.get(index).taskDone();
            //get the String with the index task marked done
            storage.save(tasks.getList());
            ui.showDone(tasks.get(index));
        }
    }
    /**
     * getter because the shortcut is private
     * @return the shortcut name
     */
    public static String getDoneShortcut() {
        return doneShortcut;
    }
    /**
     * used when the user want to change the shortcut
     * @param doneShortcut the new shortcut
     */
    public static void setDoneShortcut(String doneShortcut) {
        DoneCommand.doneShortcut = doneShortcut;
    }
}
