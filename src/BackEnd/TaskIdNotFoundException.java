package BackEnd;

public class TaskIdNotFoundException extends RuntimeException{
    private static final String MESSAGE = "Invalid task";

    TaskIdNotFoundException(){
        super(MESSAGE);
    }
}
