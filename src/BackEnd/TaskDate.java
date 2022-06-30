package BackEnd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskDate implements Comparable<TaskDate>{

    private LocalDate date;
    private static final String DATE_FORMAT = "dd/LL/yyyy";
    private DateTimeFormatter pattern = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public TaskDate(LocalDate date) throws IllegalArgumentException{
        if (date == null) {
            this.date = noDate();
        } else if(date.compareTo(LocalDate.of(1,1,1)) >= 0) {
            this.date = date;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public TaskDate(String str) throws IllegalArgumentException{
        if (str.equals("null"))
            date = noDate();
        else {
            LocalDate aux = LocalDate.parse(str, pattern);
            if (aux.compareTo(LocalDate.of(1,1,1)) >= 0)
                date = aux;
            else
                throw new IllegalArgumentException();
        }
    }

    private static LocalDate noDate() {
        return LocalDate.of(0,1,1); //There is no year 0, so we use it for noDate
    }

    private static LocalDate today(){
        return LocalDate.now();
    }

    private static LocalDate yesterday(){
        return today().minusDays(1);
    }

    private static LocalDate tomorrow(){
        return today().plusDays(1);
    }

    public LocalDate getLocalDate() {
        if (date.equals(noDate())) {
            return null;
        }
        return date;
    }

    boolean isOverdue(){
        if (date.equals(noDate()))
            return false;
        return date.compareTo(today())<0;
    }

    boolean forToday() {
        return date.compareTo(today())==0;
    }

    @Override
    public int compareTo(TaskDate o){
        return date.compareTo(o.date);
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (!(o instanceof TaskDate))
            return false;

        TaskDate aux = (TaskDate) o;
        return this.date.equals(aux.date);
    }

    @Override
    public int hashCode(){
        if (date == null)
            return 0;
        return date.hashCode();
    }

    @Override
    public String toString(){
        if(date.equals(noDate()))
            return "";
        if(date.equals(yesterday()))
            return "Yesterday";
        if(date.equals(today()))
            return "Today";
        if(date.equals(tomorrow()))
            return "Tomorrow";
        return date.format(pattern);
    }

    String saveFormat(){
        if(date.equals(noDate()))
            return "null";
        return date.format(pattern);
    }
}