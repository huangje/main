package leduc.command;

import leduc.Date;
import leduc.exception.*;
import leduc.storage.Storage;
import leduc.ui.Ui;
import leduc.task.EventsTask;
import leduc.task.Task;
import leduc.task.TaskList;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents a event task Command.
 * Allow to add a event task to the task list and to the data file.
 */
public class EventCommand extends Command {

    /**
     * static variable used for shortcut
     */
    private static String eventShortcut = "event";
    /**
     * Constructor of EventCommand.
     * @param userInput String which represent the input string of the user.
     */
    public  EventCommand(String userInput){
        super(userInput);
    }

    /**
     * Allow to add a event task to the task list and to the data file. The user can set a priority or a recurrence or both of them.
     * Recurrence only add new homework with day/week/month interval.
     * @param tasks leduc.task.TaskList which is the list of task.
     * @param ui leduc.ui.Ui which deals with the interactions with the user.
     * @param storage leduc.storage.Storage which deals with loading tasks from the file and saving tasks in the file.
     * @throws EmptyEventDateException Exception caught when the period of the event task is not given by the user.
     * @throws EmptyEventException Exception caught when the description of the event task is not given by the user.
     * @throws NonExistentDateException Exception caught when one of the two date given does not exist.
     * @throws FileException Exception caught when the file can't be open or read or modify
     * @throws ConflictDateException Exception thrown when the new event is in conflict with others event
     * @throws PrioritizeLimitException Exception caught when the new priority is greater than 9 or less than 0.
     * @throws EventDateException  Exception caught when the start date is after the end date of an event task.
     */
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws EmptyEventDateException, EmptyEventException, NonExistentDateException, FileException, ConflictDateException, PrioritizeLimitException, EventDateException, RecurrenceException, RecurrenceDateException, NegativeNumberException {
        String userSubstring;
        int nbRecurrence = 0;
        String typeOfRecurrence = "";
        if (isCalledByShortcut) {
            userSubstring = userInput.substring(EventCommand.eventShortcut.length());
        } else {
            userSubstring = userInput.substring(5);
        }
        if (userSubstring.isBlank()) {
            throw new EmptyEventException();
        }
        String[] taskDescription = userSubstring.split("/at",2);
        if (taskDescription[0].isBlank()) {
            throw new EmptyEventException();
        } else if (taskDescription.length == 1) { // no /at in input
            throw new EmptyEventDateException();
        } else {
            String description = taskDescription[0].trim();
            String periodString = taskDescription[1].trim();
            //date format used: dd/MM/yyyy HH:mm - dd/MM/yyyy HH:mm
            String[] prioritySplit = periodString.split("prio",2);
            String[] dateString = null;
            if(prioritySplit.length == 1){
                String[] recurrenceSplit = prioritySplit[0].trim().split("recu",2);
                dateString = recurrenceSplit[0].trim().split(" - ",2);
                if(!(recurrenceSplit.length==1)){
                    String[] recurrenceSplit2 = recurrenceSplit[1].trim().split(" ",2);
                    if(recurrenceSplit2.length == 1){
                        throw new RecurrenceException();
                    }
                    else {
                        typeOfRecurrence = recurrenceSplit2[0].trim();
                        if(!(typeOfRecurrence.equals("day") || typeOfRecurrence.equals("month") || typeOfRecurrence.equals("week"))){
                            throw new RecurrenceException();
                        }
                        try{
                            nbRecurrence = exceedNbRecurrence(typeOfRecurrence, Integer.parseInt(recurrenceSplit2[1].trim()), ui);
                        }catch (Exception e){
                            throw new RecurrenceException();
                        }
                        if(nbRecurrence < 0){
                            throw new NegativeNumberException();
                        }
                    }
                }
            }
            else {
                dateString = prioritySplit[0].split(" - ",2);
            }
            if (dateString.length == 1) {
                throw new EmptyEventDateException();
            } else if (dateString[0].isBlank() || dateString[1].isBlank()) {
                throw new EmptyEventDateException();
            }
            Date date1 = new Date(dateString[0].trim());
            Date date2 = new Date(dateString[1].trim());
            tasks.verifyConflictDate(date1, date2);
            if (date1.getDate().isAfter(date2.getDate())) {
                throw new EventDateException();
            }
            EventsTask newTask = null;
            if (prioritySplit.length == 1) {
                newTask = new EventsTask(description, date1, date2);
            } else {
                int priority = -1;
                String[] recurrenceSplit = prioritySplit[1].trim().split(("recu"),2);
                String priorityString = recurrenceSplit[0].trim();
                if(!(recurrenceSplit.length == 1)){
                    String[] recurrenceSplit2 = recurrenceSplit[1].trim().split(" ",2);
                    if(recurrenceSplit2.length == 1){
                        throw new RecurrenceException();
                    }
                    else {
                        typeOfRecurrence = recurrenceSplit2[0].trim();
                        if(!(typeOfRecurrence.equals("day") || typeOfRecurrence.equals("month") || typeOfRecurrence.equals("week"))){
                            throw new RecurrenceException();
                        }
                        try{
                            nbRecurrence = exceedNbRecurrence(typeOfRecurrence, Integer.parseInt(recurrenceSplit2[1].trim()), ui);
                        }catch (Exception e){
                            throw new RecurrenceException();
                        }
                        if(nbRecurrence < 0){
                            throw new NegativeNumberException();
                        }
                    }
                }
                try {
                    priority = Integer.parseInt(priorityString);
                } catch (Exception e) {
                    throw new PrioritizeLimitException();
                }
                if (priority < 1 || priority > 9) {
                    throw new PrioritizeLimitException();
                }
                newTask = new EventsTask(description, date1, date2, priority);
            }
            if (nbRecurrence == 0) {
                tasks.add(newTask);
                storage.save(tasks.getList());
                ui.showTask(newTask, tasks.size());
            }
            else {
                contructRecurrenceTask(newTask, nbRecurrence, typeOfRecurrence, tasks, storage, ui);
            }

        }
    }

    /**
     * Helper method to construct recurrence task
     * @param task the task that will be repeated
     * @param nbRecurrence the number of recurrence
     * @param typeOfRecurrence type of recurrence, can be day, week or month
     * @param tasks leduc.task.TaskList which is the list of task.
     * @param storage leduc.storage.Storage which deals with loading tasks from the file and saving tasks in the file.
     * @param ui leduc.ui.Ui which deals with the interactions with the user.
     * @throws FileException Exception caught when the file can't be open or read or modify
     * @throws RecurrenceDateException Exception caught when there is a conflict date between recurrence task
     */
    private void contructRecurrenceTask(EventsTask task, int nbRecurrence, String typeOfRecurrence, TaskList tasks, Storage storage, Ui ui) throws FileException, RecurrenceDateException {
        ArrayList<Task> newTaskList = new ArrayList<>();
        LocalDateTime initialDate1 = task.getDateFirst().getDate();
        LocalDateTime initialDate2 = task.getDateSecond().getDate();
        String description = task.getTask();
        int priority = task.getPriority();
        switch (typeOfRecurrence) {
            case "day":
                if(initialDate2.isAfter(initialDate1.plusDays(1))){
                    throw new RecurrenceDateException();
                }
                newTaskList.add(task);
                tasks.add(task);
                for (int i = 1; i <= nbRecurrence; i++) {

                    EventsTask recurrentEventsTask = new EventsTask(description, new Date(initialDate1.plusDays(i)), new Date(initialDate2.plusDays(i)), priority);
                    newTaskList.add(recurrentEventsTask);
                    tasks.add(recurrentEventsTask);
                }
                ui.showNotCompleteList(newTaskList, tasks);
                break;
            case "week":
                if(initialDate2.isAfter(initialDate1.plusWeeks(1))){
                    throw new RecurrenceDateException();
                }
                newTaskList.add(task);
                tasks.add(task);
                for (int i = 1; i <= nbRecurrence; i++) {
                    EventsTask recurrentEventsTask = new EventsTask(description, new Date(initialDate1.plusWeeks(i)), new Date(initialDate2.plusWeeks(i)), priority);
                    newTaskList.add(recurrentEventsTask);
                    tasks.add(recurrentEventsTask);
                }
                ui.showNotCompleteList(newTaskList, tasks);
                break;
            case "month":
                if(initialDate2.isAfter(initialDate1.plusMonths(1))){
                    throw new RecurrenceDateException();
                }
                for (int i = 1; i <= nbRecurrence; i++) {
                    EventsTask recurrentEventsTask = new EventsTask(description, new Date(initialDate1.plusMonths(i)), new Date(initialDate2.plusMonths(i)), priority);
                    newTaskList.add(recurrentEventsTask);
                    tasks.add(recurrentEventsTask);
                }
                newTaskList.add(task);
                tasks.add(task);
                ui.showNotCompleteList(newTaskList, tasks);
                break;
            default:
                tasks.add(task);
                ui.showTask(task, tasks.size());
                break;
        }
        storage.save(tasks.getList());
    }

    private int exceedNbRecurrence(String typeOfDate, int nbRecurrence, Ui ui) throws RecurrenceException {
        switch (typeOfDate){
            case "day":
                if(nbRecurrence > 30){
                    ui.showErrorRecurrence(typeOfDate);
                    return 30;
                }
                else {
                    return nbRecurrence;
                }
            case "week":
                if(nbRecurrence > 26){
                    ui.showErrorRecurrence(typeOfDate);
                    return 26;
                }
                else {
                    return nbRecurrence;
                }
            case "month":
                if(nbRecurrence>12){
                    ui.showErrorRecurrence(typeOfDate);
                    return 12;
                }
                else {
                    return nbRecurrence;
                }
            default: throw new RecurrenceException();
        }
    }
    /**
     * getter because the shortcut is private
     * @return the shortcut name
     */
    public static String getEventShortcut(){
        return eventShortcut;
    }

    /**
     * used when the user want to change the shortcut
     * @param eventShortcut the new shortcut
     */
    public static void setEventShortcut(String eventShortcut) {
        EventCommand.eventShortcut = eventShortcut;
    }
}
