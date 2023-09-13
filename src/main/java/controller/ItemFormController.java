package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import entity.Item;
import dto.tm.ItemTm;
import util.CrudUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ItemFormController implements Initializable {

    @FXML
    private TreeTableColumn<Object, Object> colCode;

    @FXML
    private TreeTableColumn<Object, Object> colDesc;

    @FXML
    private TreeTableColumn<Object, Object> colOption;

    @FXML
    private TreeTableColumn<Object, Object> colQtyOnHand;

    @FXML
    private TreeTableColumn<Object, Object> colUnitPrice;

    @FXML
    private AnchorPane itemPane;

    @FXML
    private Label lblCode;

    @FXML
    private JFXTreeTableView<ItemTm> tblItem;

    @FXML
    private JFXTextField txtDesc;

    @FXML
    private JFXTextField txtQty;

    @FXML
    private JFXTextField txtSearch;

    @FXML
    private JFXTextField txtUnitPrice;

    public void backButtonOnAction(ActionEvent ignoredActionEvent) {
        Stage stage = (Stage) itemPane.getScene().getWindow();
        try {
            stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../view/DashBoard.fxml")))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.show();
    }

    @FXML
    void clearButtonOnAction(ActionEvent ignoredEvent) {
        clearFields();
    }

    private void clearFields() {
        generateId();
        txtDesc.clear();
        txtUnitPrice.clear();
        txtQty.clear();
        txtSearch.clear();
        tblItem.refresh();
    }

    @FXML
    void saveButtonOnAction(ActionEvent ignoredEvent) {
        Item item = new Item(
                lblCode.getText(),
                txtDesc.getText(),
                Double.parseDouble(txtUnitPrice.getText()),
                Integer.parseInt(txtQty.getText())
        );

        try {

            boolean isSaved = CrudUtil.execute(
                    "INSERT INTO item VALUES(?,?,?,?)",
                    item.getCode(),
                    item.getDescription(),
                    item.getUnitPrice(),
                    item.getQtyOnHand()
            );

            if (isSaved) {
                new Alert(Alert.AlertType.INFORMATION,"Item has been saved successfully!").show();
                loadTable();
                clearFields();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadTable() {
        ObservableList<ItemTm> tmList = FXCollections.observableArrayList();
        try {
            List<Item> list = new ArrayList<>();

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT * FROM item"
            );

            while (resultSet.next()) {
                list.add(new Item(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getDouble(3),
                        resultSet.getInt(4)
                ));
            }

            for (Item item:list) {
                JFXButton btn = new JFXButton("Delete");
                btn.setTextFill(Color.rgb(255,255,255));
                btn.setStyle("-fx-background-color: #e35c5c; -fx-font-weight: BOLD");

                btn.setOnAction(actionEvent -> {
                    try {
                        Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to delete " + item.getCode() + " item ? ", ButtonType.YES, ButtonType.NO).showAndWait();
                        if (buttonType.get() == ButtonType.YES){

                            boolean isDeleted = CrudUtil.execute(
                                    "DELETE FROM item WHERE code=?",
                                    item.getCode()
                            );

                            if (isDeleted){
                                new Alert(Alert.AlertType.INFORMATION,"Item has been deleted successfully!").show();
                                loadTable();
                                generateId();
                            }else{
                                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
                            }
                        }
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });

                tmList.add(new ItemTm(
                        item.getCode(),
                        item.getDescription(),
                        item.getUnitPrice(),
                        item.getQtyOnHand(),
                        btn
                ));
            }

            TreeItem<ItemTm> treeItem = new RecursiveTreeItem<>(tmList, RecursiveTreeObject::getChildren);
            tblItem.setRoot(treeItem);
            tblItem.setShowRoot(false);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateId() {
        try {
            ResultSet resultSet = CrudUtil.execute(
                    "SELECT code FROM item ORDER BY code DESC LIMIT 1"
            );

            if (resultSet.next()){
                int num = Integer.parseInt(resultSet.getString(1).split("P")[1]);
                num++;
                lblCode.setText(String.format("P%03d",num));
            }else {
                lblCode.setText("P001");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateButtonOnAction(ActionEvent ignoredEvent) {
        Item item = new Item(
                lblCode.getText(),
                txtDesc.getText(),
                Double.parseDouble(txtUnitPrice.getText()),
                Integer.parseInt(txtQty.getText())
        );

        try {
            boolean isUpdate = CrudUtil.execute(
                    "UPDATE item SET description=? , unitPrice=?, qtyOnHand=? WHERE code=?",
                    item.getCode(),
                    item.getDescription(),
                    item.getUnitPrice(),
                    item.getQtyOnHand()
            );

            if (isUpdate){
                new Alert(Alert.AlertType.INFORMATION,"Item has been updated successfully!").show();
                clearFields();
                loadTable();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colCode.setCellValueFactory(new TreeItemPropertyValueFactory<>("code"));
        colDesc.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("unitPrice"));
        colQtyOnHand.setCellValueFactory(new TreeItemPropertyValueFactory<>("qtyOnHand"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));
        generateId();
        loadTable();

        tblItem.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->{
            if (newValue!=null){
                setData(newValue);
            }
        });

        txtSearch.textProperty().addListener((observableValue, oldValue, newValue) -> tblItem.setPredicate(treeItem -> treeItem.getValue().getCode().contains(newValue) ||
                treeItem.getValue().getDescription().contains(newValue)));

    }

    private void setData(TreeItem<ItemTm> value) {
        lblCode.setText(value.getValue().getCode());
        txtDesc.setText(value.getValue().getDescription());
        txtUnitPrice.setText(String.valueOf(value.getValue().getUnitPrice()));
        txtQty.setText(String.valueOf(value.getValue().getQtyOnHand()));
    }
}
