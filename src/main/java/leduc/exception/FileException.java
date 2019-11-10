package leduc.exception;

/**
 * Thrown when the file can't be opened, read or written
 */
public class FileException extends DukeException {
    /**
     * Constructor of leduc.exception.DukeException
     */
    public FileException(){
        super();
    }

    @Override
    /**
     * There are some problem with the file
     * @return the error message
     */
    public String print() {
        return "File doesn't exist or cannot be created or cannot be opened";
    }
}
