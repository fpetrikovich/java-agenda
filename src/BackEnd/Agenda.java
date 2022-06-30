package BackEnd;

import java.io.*;
import java.time.DateTimeException;
import java.util.*;
import java.util.stream.Collectors;

public class Agenda {
    private Set<Task> tasksAgenda = new TreeSet<>();
    private Integer idx = 1;

    public Integer add(String description, TaskDate date){
        tasksAgenda.add(new Task(idx, description, date));
        idx++;
        return idx - 1;
    }

    public Integer getIdx(){
        return idx;
    }

    public void edit(Integer id, String description, TaskDate date) {
        delete(id);
        tasksAgenda.add(new Task(id, description, date));
    }

    public void delete(Integer id){
        tasksAgenda.remove(withID(id));
    }

    private Task withID(Integer id){
        return tasksAgenda.stream().filter(task -> task.getId().equals(id)).collect(Collectors.toList()).get(0);
    }

    public void complete(Integer id) {
        if (tasksAgenda.stream().noneMatch(task -> task.getId().equals(id))){
            throw new TaskIdNotFoundException();
        }
        withID(id).complete();
    }

    public void uncomplete(Integer id) {
        if (tasksAgenda.stream().noneMatch(task -> task.getId().equals(id))) {
            throw new TaskIdNotFoundException();
        }
        withID(id).uncomplete();
    }

    public List<Task> findText(String text){
        return tasksAgenda
                .stream()
                .filter(task -> task.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Task> listAll(){
        return new ArrayList<>(tasksAgenda);
    }

    public List<Task> listDueToday (){
        return tasksAgenda.stream().filter(task -> task.getDate().forToday()).collect(Collectors.toList());
    }
    public List<Task> listOverdue(){
        return tasksAgenda.stream().filter(task -> task.getDate().isOverdue()).collect(Collectors.toList());
    }

    public List<Task> archiveCompleted(){
        if(tasksAgenda.removeAll(tasksAgenda.stream().filter(task -> task.isComplete()).collect(Collectors.toList()))) {
            return new ArrayList<>(tasksAgenda);
        }
        return this.listAll();
    }
    public List<Task> archiveOverdue(){
        if(tasksAgenda.removeAll(this.listOverdue())){
            return new ArrayList<>(tasksAgenda);
        }
        return this.listAll();
    }

    @Override
    public String toString(){
        StringBuilder list = new StringBuilder("Agenda con:\n");
        for(Task task:tasksAgenda){
            list.append(task.toString());
        }
        return list.toString();
    }

    public String save(File f){
        String s;
        String value = "";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(idx.toString(), 0, idx.toString().length());
            writer.write('\n');
            for (Task task : tasksAgenda) {
                s = task.saveFormat();
                writer.write(s, 0, s.length());
            }
            writer.close();
            return value;
        } catch (IOException x) {
            value = String.format("IO Exception: %s%n", x);
            System.err.format(value);
            return value;
        }

    }

    public String load(File f){
        String line;
        String[] parts;
        Set<Task> aux = new TreeSet<>();
        String message = "";

        try{
            BufferedReader reader = new BufferedReader(new FileReader(f));
            line = reader.readLine().trim();
            int id = Integer.parseInt(line);
            if (id < 1)
                return "Unexpected value for Internal Id counter.";
            while ((line = reader.readLine()) != null){
                parts = line.split(",",4);
                int taskId = Integer.parseInt(parts[0].trim());
                if (parts[3].trim().equals(""))
                    return "Found a Task with no description.";
                if (taskId >= id || taskId < 1)
                    return "Unexpected values in the Id of one or more Tasks.";

                Task t = new Task(taskId, parts[3].trim(), new TaskDate(parts[2].trim()));
                if (parts[1].toLowerCase().trim().equals("true"))
                    t.complete();
                aux.add(t);
            }
            tasksAgenda = aux;
            idx = id;
            return message;
        }catch(IOException x){
            return message = String.format("IO Exception: %s%n",x);
        }catch(NumberFormatException | DateTimeException x){
            return message = String.format("%s",x);
        } catch(IllegalArgumentException x){
            return message = String.format("IllegalArgument Exception: %s",x);
        }finally{
            System.err.format(message);
        }
    }
}

