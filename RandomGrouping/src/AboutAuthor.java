/*
 * Created by WuSongYe on 2023/10/28
 * PhotoViewer
 */

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutAuthor extends Application {
    private static final String author = "吴松烨";
    private static final String studentsNumber="0224766";
    private VBox vBox=new VBox(20);
    private Label authorInfo;
    private Label projectInfo;
    private Hyperlink myGithubAddress=new Hyperlink("项目地址");
    private Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        authorInfo = new Label(studentsNumber+author);
        projectInfo=new Label();
        projectInfo.setText("随机分组小程序 您可以在导入数据后的表格中右击\n在菜单中选择删除或添加记录\n您也可以在表格中直接编辑组名和分数");
        vBox.getChildren().addAll(authorInfo,projectInfo,myGithubAddress);
        BorderPane borderPane=new BorderPane();
        borderPane.setCenter(vBox);
        vBox.setAlignment(Pos.CENTER);
        scene=new Scene(borderPane,300,120);
        primaryStage.setScene(scene);
        primaryStage.setTitle("RandomGrouping");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.show();
        myGithubAddress.setOnAction(e -> {
            HostServices host=getHostServices();
            host.showDocument("https://github.com/Rayccyl/RandomGrouping");
        });
    }
}
