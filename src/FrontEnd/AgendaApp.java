package FrontEnd;
import BackEnd.Agenda;
import BackEnd.Task;
import BackEnd.TaskDate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class AgendaApp extends Application {

    private Agenda taskSet = new Agenda();
    private ObservableList<Task> data = FXCollections.observableList(taskSet.listAll());
    private TableView tableView;
    private TextField searchBar;
    private ComboBox<String> listComboBox;
    private final String PATTERN = "dd/MM/yyyy";
    private final String OVERDUE = "Overdue";
    private final String DUETODAY = "Due today";


    @Override
    public void start(Stage primaryStage){

        //Main Menu Bar
        MenuBar mainMenu = new MenuBar();

        //--File Menu
        Menu file = new Menu("File");
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem saveMenuItem = new MenuItem("Save");
        MenuItem exitMenuItem = new MenuItem("Exit");
        
        //----Adding the items to the File Menu
        file.getItems().addAll(openMenuItem, saveMenuItem,exitMenuItem);

        //--Archive Menu
        Menu archive = new Menu("Archive");
        MenuItem completedMenuItem = new MenuItem("Completed");
        MenuItem overdueMenuItem = new MenuItem("Overdue");

        //----Adding the items to the Archive Menu
        archive.getItems().addAll(completedMenuItem, overdueMenuItem);

        //--Adding the menu to the Main Menu Bar
        mainMenu.getMenus().addAll(file, archive);

        //General HBox
        HBox generalHBox = new HBox();
        generalHBox.setSpacing(300);
        generalHBox.setPadding(new Insets(5,2,5,2));
        generalHBox.setAlignment(Pos.CENTER);

        //--Search HBox
        HBox hBoxSearch = new HBox();
        hBoxSearch.setSpacing(10);

        //----Search Bar
        searchBar = new TextField();
        searchBar.setPromptText("Search");

        //----Combo Box
        listComboBox = new ComboBox<>();
        listComboBox.setMaxWidth(150);
        listComboBox.setPromptText("All");
        listComboBox.getItems().addAll("All", DUETODAY, OVERDUE);

        //----Adding Search Bar and Combo Box to Search HBox
        hBoxSearch.getChildren().addAll(searchBar, listComboBox);

        //--Tasks HBox
        HBox hBoxTasks = new HBox();
        hBoxTasks.setSpacing(7);

        //----Tasks Buttons
        Button newButton = new Button("New");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        //----Adding Buttons to Tasks HBox
        hBoxTasks.getChildren().addAll(newButton, editButton, deleteButton);

        //--Adding Boxes to General Box
        generalHBox.getChildren().addAll(hBoxSearch, hBoxTasks);


        //Table View
        tableView = new TableView();
        tableView.setItems(data);
        tableView.setEditable(true);
        tableView.prefHeightProperty().bind(primaryStage.heightProperty().subtract(120));


        //--Description Column
        StringConverter<Object> sc = new StringConverter<Object>() {
            @Override
            public String toString(Object t) {
                return t == null ? null : t.toString();
            }

            @Override
            public Object fromString(String string) {
                return string;
            }
        };
        TableColumn descrCol = new TableColumn();
        descrCol.setText("Description");
        descrCol.setCellValueFactory(new PropertyValueFactory("description"));
        descrCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
	    descrCol.setSortable(false);
	    descrCol.prefWidthProperty().bind(primaryStage.widthProperty().subtract(230));

        //--Due Column
        TableColumn<Task, TaskDate> dueCol = new TableColumn<>();
        dueCol.setText("Due");
        dueCol.setMinWidth(130);
        dueCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        dueCol.setCellFactory(column -> {
            return new TableCell<Task, TaskDate>() {
                @Override
                protected void updateItem(TaskDate item, boolean empty) {
                    super.updateItem(item,empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        setStyle("");
                    }
                }
            };
        });
	    dueCol.setSortable(false);

        //--Status Column
        TableColumn statusCol = new TableColumn<>();
        statusCol.setText("Done");
        statusCol.setMinWidth(70);
        statusCol.setCellValueFactory(new PropertyValueFactory<Task,Boolean>("status"));
        statusCol.setCellFactory(CheckBoxTableCell.forTableColumn(statusCol));
	    statusCol.setSortable(false);

        //--Adding Columns to Table View
        tableView.getColumns().addAll(descrCol,dueCol, statusCol);

        //FileChooser
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);


        //Events
        //--When you press Save Menu Item
        saveMenuItem.setOnAction(event -> {
            File f = fileChooser.showSaveDialog(primaryStage);
            String message;

            if(f != null){
                message = taskSet.save(f);
                if (message.equals("")){
                    informationAlert("File Save","The file was saved successfully!","Click ok to close window.");
                } else
                    errorAlert("File Save", "There was an error with the file",message);
            }else
                informationAlert("File Save","The file was not saved","No changes were made.");
        });

        //--When you press Open Menu Item
        openMenuItem.setOnAction(event -> {
            File f = fileChooser.showOpenDialog(primaryStage);
            String message;

            if(f != null && confirmationAlert("Confirmation", "Are you sure you want to open this file?", "All current tasks will be lost.")){
                message = taskSet.load(f);
                if (message.equals("")) {
                    data = FXCollections.observableList(taskSet.listAll());
                    resetTable();
                    informationAlert("File Open", "The file as opened successfully!","Click ok to close window.");
                } else
                    errorAlert("File Open","There was an error with the file",message);
            }else
                informationAlert("File Open", "The file was not opened", "No changes were made.");
        });

        //--When you press Exit Menu Item
        exitMenuItem.setOnAction(event -> {
            if (confirmationAlert("Confirmation", "Are you sure you want to exit?", "All unsaved tasks will be lost.")) {
                Platform.exit();
            }
        });

        //--When you press Archive Completed Menu Item
        completedMenuItem.setOnAction(event -> {
            data = FXCollections.observableList(taskSet.archiveCompleted());
            resetTable();
            resetSearchBar();
            resetComboBox();
            informationAlert("Information","Completed task archived", "");
        });

        //--When you press Archive Overdue Menu Item
        overdueMenuItem.setOnAction(event -> {
            data = FXCollections.observableList(taskSet.archiveOverdue());
            resetTable();
            resetSearchBar();
            resetComboBox();
            informationAlert("Information","Overdue task archived", "");
        });

        //--When you type on SearchBar
        searchBar.setOnKeyReleased(e -> {
            if (!"All".equals(listComboBox.getValue())) {
                resetComboBox();
                searchBar.insertText(0,e.getText());
            }
            data = FXCollections.observableList(taskSet.findText(searchBar.getText()));
            resetTable();
        });

        //--When you press on Item on List Combo Box
        listComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener( (ObservableValue<? extends String> observable, String oldOption, String newOption) -> {
                    switch (newOption){
                        case "All": data = FXCollections.observableList(taskSet.listAll()); break;
                        case "Due today": data = FXCollections.observableList(taskSet.listDueToday()); break;
                        case "Overdue": data = FXCollections.observableList(taskSet.listOverdue()); break;
                    }
                    resetTable();
                    resetSearchBar();
                });

        //--When you press New
        newButton.setOnAction(new NewHandler());

        //--When you press Edit
        editButton.setOnAction(new EditHandler());

        //--When you press Delete
        deleteButton.setOnAction(new DeleteHandler());

        //--When you check Status Column
        statusCol.setOnEditCommit(new StatusHandler());

        //Creating the layout
        VBox vBox = new VBox();
        vBox.getChildren().addAll(mainMenu, generalHBox, tableView);

        //Creating the scene
        Scene scene = new Scene(vBox, 800, 600);

        //Launching Stage
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    //Methods
    private void resetTable() {
        tableView.setItems(data);
        tableView.refresh();
    }
    
    private void resetSearchBar() {
        searchBar.setText("");
    }
    
    private void resetComboBox() {
        listComboBox.setValue(listComboBox.getPromptText());
    }

    private void refresh() {
        String aux = listComboBox.getSelectionModel().getSelectedItem();
        switch (aux) {
            case DUETODAY: data = FXCollections.observableList(taskSet.listDueToday());break;
            case OVERDUE: data = FXCollections.observableList(taskSet.listOverdue());break;
            default: break;
        }
        if (aux.equals(listComboBox.getPromptText())) {
            aux = searchBar.getText();
            if (aux.equals("")) {
                data = FXCollections.observableList(taskSet.listAll());
            } else {
                data = FXCollections.observableList(taskSet.findText(aux));
            }
        }
        tableView.setItems(data);
        tableView.refresh();
    }

    private void errorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                alert.close();
            }
        }
    }

    private Boolean confirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                return true;
            }
            return false;
        }
        return false;
    }

    private void informationAlert(String title, String header, String content) {
        Alert inform = new Alert(Alert.AlertType.INFORMATION);
        inform.setTitle(title);
        inform.setHeaderText(header);
        inform.setContentText(content);
        Optional<ButtonType> result = inform.showAndWait();
        if(result.isPresent()) {
            inform.close();
        }
    }

    //Handlers
    private class NewHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {

            //Layout of the window
            VBox newTaskLayout = new VBox();

            //Names of the fields
            Text idLabel = new Text("ID");
            Text descriptionLabel = new Text("Description");
            Text dateLabel = new Text("Due");

            //Fields
            Text idValue = new Text(String.valueOf(taskSet.getIdx()));
            TextField descriptionText = new TextField();
            DatePicker datePicker = new DatePicker();

            //Setting Date Picker format to PATTERN
            datePicker.setPromptText(PATTERN.toLowerCase());
            datePicker.setConverter(new AgendaApp.Converter());

            //Creating a Grid Pane
            GridPane gridPane = new GridPane();
            gridPane.setMinSize(300, 200);
            gridPane.setPadding(new Insets(10, 10, 10, 10));
            gridPane.setVgap(30);
            gridPane.setHgap(10);
            gridPane.setAlignment(Pos.CENTER);

            //Arranging all the nodes in the GridPlane
            gridPane.add(idLabel,0,0);
            GridPane.setHalignment(idLabel, HPos.CENTER);
            gridPane.add(idValue, 1, 0);
            gridPane.add(descriptionLabel,0,1);
            GridPane.setHalignment(descriptionLabel, HPos.CENTER);
            gridPane.add(descriptionText,1,1);
            gridPane.add(dateLabel,0,2);
            GridPane.setHalignment(dateLabel, HPos.CENTER);
            gridPane.add(datePicker,1,2);

            //Creating the save and cancel buttons
            Button saveButton = new Button("Save");
            Button cancelButton = new Button("Cancel");

            //Position of the buttons
            HBox buttonBox = new HBox();
            buttonBox.getChildren().addAll(cancelButton, saveButton);
            buttonBox.setSpacing(15);
            buttonBox.setAlignment(Pos.BASELINE_RIGHT);
            buttonBox.setPadding(new Insets(10,10,10,10));

            //Grouping everything in the layout
            newTaskLayout.getChildren().addAll(gridPane, buttonBox);

            //Creating the scene
            Scene secondScene = new Scene(newTaskLayout);

            // New window (primaryStage)
            Stage newWindow = new Stage();
            newWindow.setTitle("New Task");
            newWindow.setScene(secondScene);

            newWindow.show();

            //Events
            //--When Cancel is pressed
            cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    newWindow.close();
                }
            });

            //--When Save is pressed
            saveButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    String auxDescr = descriptionText.getText();

                    if (!auxDescr.equals("")){
                        taskSet.add(auxDescr, new TaskDate(datePicker.getValue()));
                        data = FXCollections.observableList(taskSet.listAll());
                        resetTable();
                        resetComboBox();
                        resetSearchBar();
                        newWindow.close();
                    } else {
                        errorAlert("Unable to create task", "Values for the task are incorrect", "Make sure to add a description");
                    }
                }
            });

        }
    }

    private class DeleteHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            Task selectedItem = (Task) tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (confirmationAlert("Confirmation", "Are you sure you want to delete the item?", "This change is irreversile")) {
                    taskSet.delete(selectedItem.getId());
                    data.remove(selectedItem);
                    resetTable();
                }
            }
        }
    }

    private class EditHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            //Get selected item
            Task selectedItem = (Task) tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                //Layout of the window
                VBox newTaskLayout = new VBox();

                //Names of the fields
                Text idLabel = new Text("ID");
                Text descriptionLabel = new Text("Description");
                Text dateLabel = new Text("Due");
                Text statusLabel = new Text("Status");

                //Fields
                Text idValue = new Text(String.valueOf(selectedItem.getId()));
                TextField descriptionText = new TextField();
                descriptionText.setText(selectedItem.getDescription());
                DatePicker datePicker = new DatePicker();
                datePicker.setValue(selectedItem.getDate().getLocalDate());
                ChoiceBox<String> statusBox = new ChoiceBox<>();
                statusBox.getItems().addAll("Pending", "Completed");
                statusBox.setValue(selectedItem.isComplete() ? "Completed" : "Pending");

                //Setting Date Picker format to PATTERN
                datePicker.setPromptText(PATTERN.toLowerCase());
                datePicker.setConverter(new AgendaApp.Converter());

                //Creating a Grid Pane
                GridPane gridPane = new GridPane();
                gridPane.setMinSize(300, 200);
                gridPane.setPadding(new Insets(10, 10, 10, 10));
                gridPane.setVgap(30);
                gridPane.setHgap(10);
                gridPane.setAlignment(Pos.CENTER);

                //Arranging all the nodes in the GridPlane
                gridPane.add(idLabel,0,0);
                GridPane.setHalignment(idLabel, HPos.CENTER);
                gridPane.add(idValue, 1, 0);
                gridPane.add(descriptionLabel,0,1);
                GridPane.setHalignment(descriptionLabel, HPos.CENTER);
                gridPane.add(descriptionText,1,1);
                gridPane.add(dateLabel,0,2);
                GridPane.setHalignment(dateLabel, HPos.CENTER);
                gridPane.add(datePicker,1,2);
                gridPane.add(statusLabel,0,3);
                GridPane.setHalignment(statusLabel, HPos.CENTER);
                gridPane.add(statusBox,1,3);

                //Creating the save and cancel buttons
                Button saveButton = new Button("Save");
                Button cancelButton = new Button("Cancel");

                //Position of the buttons
                HBox buttonBox = new HBox();
                buttonBox.getChildren().addAll(cancelButton, saveButton);
                buttonBox.setSpacing(15);
                buttonBox.setAlignment(Pos.BASELINE_RIGHT);
                buttonBox.setPadding(new Insets(10,10,10,10));


                //Grouping everything in the layout
                newTaskLayout.getChildren().addAll(gridPane, buttonBox);

                //Creating the scene
                Scene secondScene = new Scene(newTaskLayout);

                // New window (primaryStage)
                Stage newWindow = new Stage();
                newWindow.setTitle("New Task");
                newWindow.setScene(secondScene);

                newWindow.show();

                //Closing the window when Cancel is pressed
                cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        newWindow.close();
                    }
                });

                //Editing Task when Save is pressed
                saveButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String auxDescr = descriptionText.getText();

                        if (!auxDescr.equals("")){
                            taskSet.edit(selectedItem.getId(), auxDescr, new TaskDate(datePicker.getValue()));
                            String selectedStatus = statusBox.getSelectionModel().getSelectedItem();
                            switch (selectedStatus) {
                                case "Completed": taskSet.complete(selectedItem.getId()); break;
                                case "Pending": taskSet.uncomplete(selectedItem.getId()); break;
                                default: taskSet.uncomplete(selectedItem.getId()); break;
                            }
                            data = FXCollections.observableList(taskSet.listAll());
                            refresh();
                            newWindow.close();
                        } else {
                            //Error message
                            errorAlert("Unable to edit task", "Values for the task are incorrect", "Make sure to add a description");
                        }
                    }
                });
            }
        }
    }

    private class StatusHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle (ActionEvent event) {
            Task selectedItem = (Task) tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (!selectedItem.isComplete()) {
                    taskSet.complete(selectedItem.getId());
                } else {
                    taskSet.uncomplete(selectedItem.getId());
                }
                resetTable();
            }
        }
    }

    private class Converter extends StringConverter<LocalDate>{
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(PATTERN);

        @Override
        public String toString(LocalDate date) {
            if (date != null) {
                return dateFormatter.format(date);
            } else {
                return "";
            }
        }

        @Override
        public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {
                return LocalDate.parse(string, dateFormatter);
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
