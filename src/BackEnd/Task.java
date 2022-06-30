package BackEnd;

import javafx.beans.property.*;

public class Task implements Comparable<Task> {
    private BooleanProperty status;
    private ObjectProperty<TaskDate> date;
    private IntegerProperty id;
    private StringProperty description;

    Task(Integer id, String description, TaskDate date) {
        this.id = new SimpleIntegerProperty(id);
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleObjectProperty<TaskDate>(date);
        this.status = new SimpleBooleanProperty(false);
    }

    public TaskDate getDate() {
        return date.getValue();
    }

    public String getDescription() {
        return description.getValue();
    }

    public Integer getId() {
        return id.getValue();
    }

    public Boolean getStatus() {
        return status.getValue();
    }

    public void complete() {
        this.status = new SimpleBooleanProperty(true);
    }

    public void uncomplete() {
        this.status = new SimpleBooleanProperty(false);
    }

    public boolean isComplete(){
        return status.getValue();
    }

    @Override
    public int compareTo(Task other) {
        if (getId().equals(other.getId()))
            return 0;
        int comp = getDate().compareTo(other.getDate());
        if (comp != 0) {
            return comp;
        }
        comp = getDescription().compareTo(other.getDescription());
        return comp == 0 ? getId().compareTo(other.getId()) : comp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task that = (Task) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode(){
        return getId();
    }

    @Override
    public String toString(){
        return String.format("%d, %s, %s, %s\n",getId(),getStatus(),getDate(),getDescription());
    }

    String saveFormat(){
        return String.format("%d, %s, %s, %s\n",getId(),getStatus(),getDate().saveFormat(),getDescription());
    }

    //Funciones para TableView
    public BooleanProperty statusProperty() { return status; }

    public StringProperty descriptionProperty() { return description; }

    public IntegerProperty idProperty() { return id; }

    public ObjectProperty<TaskDate> dateProperty() { return date; }
}
