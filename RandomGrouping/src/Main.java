import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/*
 * Created by WuSongYe on 2023/11/3
 * Default (Template) Project
 */

public class Main extends Application {
    static class Candidate {
        String name;
        int score;
        int groupNum;

        public Candidate(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String toString() {
            return "Candidate[" +
                    "name='" + name + '\'' +
                    ", score=" + score +
                    ", groupNum=" + groupNum +
                    ']';
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public int getGroupNum() {
            return groupNum;
        }

        public void setGroupNum(int group) {
            groupNum = group;
        }

        public void setName(String newValue) {
            name = newValue;
        }

        public void setScore(Number newValue) {
            score = newValue.intValue();
        }
    }

    ArrayList<Candidate> imported = new ArrayList<>();
    ArrayList<Candidate> grouped = new ArrayList<>();
    public MenuBar menuBar = new MenuBar();
    public Menu list = new Menu("名单");
    public Menu group = new Menu("分组");
    public Menu about = new Menu("关于");
    public MenuItem importList = new MenuItem("导入名单");
    public MenuItem pasteList = new MenuItem("粘贴名单");
    public MenuItem startGrouping = new MenuItem("开始分组");
    public MenuItem copyList = new MenuItem("复制名单");
    public MenuItem exportList = new MenuItem("导出名单");
    public MenuItem aboutProject = new MenuItem("关于项目");
    public MenuItem clearList = new MenuItem("清除名单");
    public RadioMenuItem withSeed = new RadioMenuItem("种子分组");
    public boolean hasSeed = false;
    public static int groupNum = 1;
    public Button moreGroup = new Button("↑");
    public Button lessGroup = new Button("↓");
    public VBox vbox = new VBox(moreGroup, lessGroup);
    public Label currentGroupNum = new Label(String.valueOf(groupNum));
    public TableView tableView = new TableView();
    public HBox numControl = new HBox(30, currentGroupNum, vbox);
    public int[] groupNumEachItems;

    public CustomMenuItem groupNumUpdate = new CustomMenuItem(numControl);
    TableColumn<Candidate, String> columnTeam = new TableColumn("Team");
    TableColumn<Candidate, Number> columnScore = new TableColumn("Score");
    TableColumn<Candidate, Number> columnGroupNum = new TableColumn("Group");
    char alphaChar = 'A';
    public static void main(String[] args) {
        System.out.println("Loading");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        currentGroupNum.setFont(new Font("JetBrains Mono", 40));
        currentGroupNum.setTextFill(Color.BLACK);
        groupNumUpdate.setContent(numControl);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        currentGroupNum.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER_LEFT);
        menuBar.getMenus().addAll(list, group,about);
        list.getItems().addAll(importList, pasteList, copyList, exportList, clearList);
        group.getItems().addAll(startGrouping, withSeed, groupNumUpdate);
        about.getItems().addAll(aboutProject);
        copyList.setDisable(true);
        exportList.setDisable(true);
        startGrouping.setDisable(true);
        borderPane.setCenter(tableView);
        borderPane.setPadding(new Insets(10));
        groupNumUpdate.setHideOnClick(false);
        Scene scene = new Scene(borderPane, 500, 400);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setTitle("随机分组");
        primaryStage.setScene(scene);
        primaryStage.show();

        tableView.setEditable(true);

        columnTeam.setCellValueFactory(param -> {

            SimpleStringProperty name = new SimpleStringProperty(param.getValue().getName());
            return name;
        });
        columnTeam.setCellFactory(TextFieldTableCell.forTableColumn());
        columnTeam.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());
        });
        columnTeam.setPrefWidth(tableView.getWidth()/3);
        columnScore.setCellValueFactory(param -> {
            SimpleIntegerProperty SimpleScore = new SimpleIntegerProperty(param.getValue().getScore());
            return SimpleScore;
        });
        columnScore.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        columnScore.setOnEditCommit(event -> {
            Candidate candidate = event.getRowValue();
            candidate.setScore(event.getNewValue());
        });
        columnScore.setPrefWidth(tableView.getWidth()/3);
        columnGroupNum.setCellValueFactory(param -> {
            SimpleIntegerProperty SimpleScore = new SimpleIntegerProperty(param.getValue().getGroupNum());
            return SimpleScore;
        });
        columnGroupNum.setPrefWidth(tableView.getWidth()/3);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeRecord = new MenuItem("删除记录");
        removeRecord.setOnAction(event -> {
            Candidate selectedCandidate = (Candidate) tableView.getSelectionModel().getSelectedItem();
            tableView.getItems().remove(selectedCandidate);
            imported.remove(selectedCandidate);
        });
        MenuItem addRecord = new MenuItem("新增记录");
        addRecord.setOnAction(event -> {
            Candidate candidate = new Candidate("请输入", 0);
            tableView.getItems().add(candidate);
            imported.add(candidate);
        });
        contextMenu.getItems().addAll(removeRecord, addRecord);
        tableView.setContextMenu(contextMenu);
        tableView.getColumns().addAll(columnTeam, columnScore, columnGroupNum);
        Consumer<String> load = line -> {
            String[] tmp = line.split(",");
            Candidate candidate = new Candidate(tmp[0], Integer.parseInt(tmp[1]));
            imported.add(candidate);
        };

        importList.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if(file == null){
                return;
            }
            try (Scanner scanner = new Scanner(file, "UTF-8")) {
                importData(tableView, load, scanner);

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("格式要求如\n中国,24\n韩国38");
                alert.setTitle("导入错误 请检查文件格式");
                alert.showAndWait();
            }
            if(imported.size()!=0){
                copyList.setDisable(false);
                exportList.setDisable(false);
                startGrouping.setDisable(false);
            }
        });
        pasteList.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                InputStream inputStream = new ByteArrayInputStream((clipboard.getString().getBytes(StandardCharsets.UTF_8)));
                Scanner scanner = new Scanner(inputStream, "UTF-8");
                importData(tableView, load, scanner);

            } else {
                return;
            }
            if(imported.size()!=0){
                copyList.setDisable(false);
                exportList.setDisable(false);
                startGrouping.setDisable(false);
            }
        });
        exportList.setOnAction(event -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data.csv"), StandardCharsets.UTF_8)))  {
                ObservableList<Candidate> items = tableView.getItems();
                for (Candidate candidate : items) {
                    writer.write(candidate.getName() + "," + candidate.getScore() + "," + candidate.getGroupNum() + "\n");
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("写入成功");
                alert.setContentText("已将结果输出至data.csv 文件与.jar在同一目录下 UTF-8格式");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        copyList.setOnAction(event -> {
            if (tableView.getItems().size() > 0) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                ObservableList<Candidate> items = tableView.getItems();
                StringWriter writer = new StringWriter();
                for (Candidate candidate : items) {
                    writer.write(candidate.getName() + "," + candidate.getScore() + "," + candidate.getGroupNum() + "\n");
                }
                content.putString(writer.toString());
                clipboard.setContent(content);
            } else {
                System.out.println("Clipboard does not contain text.");
            }
        });
        clearList.setOnAction(event -> {
            imported.clear();
            grouped.clear();
            tableView.getItems().clear();
        });
        startGrouping.setOnAction(event -> {
            alphaChar = 'A';
            randomGrouping(hasSeed);
            analysis(primaryStage.getY());
        });
        moreGroup.setOnAction(event -> {
            if (groupNum < imported.size()) {
                groupNum++;
                currentGroupNum.setText(String.valueOf(groupNum));
            }
        });
        lessGroup.setOnAction(event -> {
            if (groupNum > 1) {
                groupNum--;
                currentGroupNum.setText(String.valueOf(groupNum));
            }
        });
        withSeed.setOnAction(event -> {
            hasSeed = withSeed.isSelected();
        });
        aboutProject.setOnAction(event -> {
            try {
                new AboutAuthor().start(new Stage());
            } catch (Exception e) {
                return;
            }
        });
    }

    private void importData(TableView tableView, Consumer<String> load, Scanner scanner) {
        imported.clear();
        grouped.clear();
        if(!scanner.hasNextLine()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("格式要求如\n中国,24\n韩国38");
            alert.setTitle("导入错误 请检查文件格式");
            alert.showAndWait();
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            load.accept(line);
        }
        imported.forEach(System.out::println);
        ObservableList<Candidate> candidates = FXCollections.observableArrayList(imported);
        tableView.getItems().addAll(candidates);
        scanner.close();
    }
    private void randomGrouping(boolean withSeed) {
        int[] groupNumEachItems = new int[groupNum];
        int ItemsInAll = imported.size();
        int restItems = ItemsInAll;
        int restGroup = groupNum;
        for (int i = 0; i < groupNum; i++) {
            groupNumEachItems[i] = (int) Math.ceil((float) restItems / restGroup);
            if (restItems % restGroup == 0) groupNumEachItems[i] = restItems / restGroup;
            restItems -= groupNumEachItems[i];
            restGroup--;
        }
        this.groupNumEachItems=groupNumEachItems.clone();
        PriorityQueue<Candidate> maxHeap = new PriorityQueue<>((a, b) -> Float.compare(a.getScore() + 0.1f, b.getScore()));
        if (withSeed) {
            for (Candidate candidate : imported) {
                maxHeap.offer(candidate);
                if (maxHeap.size() > groupNum) {
                    maxHeap.poll();
                }
            }
            int i = 0;
            for (Candidate candidate : maxHeap) {
                candidate.setGroupNum(i + 1);
                groupNumEachItems[i]--;
                i++;
            }
        }

        Random random = new Random();
        grouped = (ArrayList<Candidate>) imported.clone();
        for (Candidate candidate : grouped) {
            int index = 0;
            if(maxHeap.contains(candidate)){
                continue;
            }
            while (true) {
                if (groupNumEachItems[index = random.nextInt(groupNum)] > 0) break;
            }
            candidate.setGroupNum(index + 1);
            groupNumEachItems[index]--;
        }
        ObservableList<Candidate> candidates = FXCollections.observableArrayList(grouped);
        tableView.getItems().clear();
        tableView.getItems().addAll(candidates);
        tableView.getSortOrder().addAll(columnGroupNum, columnScore);
        columnGroupNum.setSortType(TableColumn.SortType.ASCENDING);
        columnScore.setSortType(TableColumn.SortType.DESCENDING);
        tableView.sort();
    }
    private void analysis(double y) {

        Stage chartStage = new Stage();
        chartStage.setTitle("Charts");
        TilePane tilePane = new TilePane(Orientation.VERTICAL);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        ScrollPane scrollPane = new ScrollPane(tilePane);
        BarChart<Number, String> barCharts[]=new BarChart[groupNum];
        for(int i=0;i<groupNum;i++){
            barCharts[i]=createBarChart(i+1);
            alphaChar++;
        }
        int j=0;
        for (int i = 0; i < Math.ceil(groupNum/2.0f); i++) {
            HBox hBox = new HBox(36);
            hBox.getChildren().add(barCharts[j]);
            if(j!=groupNum-1)hBox.getChildren().add(barCharts[++j]);
            tilePane.getChildren().add(hBox);
            j++;
        }
        Scene chartScene = new Scene(scrollPane, 900, 750);
        chartStage.setScene(chartScene);
        chartStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        chartStage.setTitle("分组情况");
        chartStage.setY(y-60);
        chartStage.show();
    }
    private BarChart<Number, String> createBarChart(int index) {
        ObservableList<XYChart.Series<Number, String>> barChartData = generateData(index);
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis, barChartData);
        barChart.setTitle(alphaChar+"组");
        barChart.setBarGap(3);
        barChart.setCategoryGap(20);
        return barChart;
    }
    private ObservableList<XYChart.Series<Number, String>> generateData(int index) {
        ObservableList<XYChart.Series<Number, String>> barChartData = FXCollections.observableArrayList();
        for (int i = 1; i <= groupNumEachItems[index-1]; i++) {
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            int times=i-1;
            for(Candidate candidate:grouped) {
                if(candidate.getGroupNum()==index) {
                    if(times==0) {
                        series.getData().add(new XYChart.Data<>(candidate.getScore(), candidate.getName()));
                        series.setName(candidate.name);
                        break;
                    }else times--;
                }
            }
            barChartData.add(series);
        }

        return barChartData;
    }
}