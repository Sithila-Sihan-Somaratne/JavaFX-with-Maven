package controller;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import entity.Order;
import entity.OrderDetails;
import dto.tm.CartTm;
import util.CrudUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class PlaceOrderFormController implements Initializable {
    @FXML
    private JFXComboBox<String> cmbCustomerId;

    @FXML
    private JFXComboBox<String> cmbItemCode;

    @FXML
    private TreeTableColumn<Object, Object> colAmount;

    @FXML
    private TreeTableColumn<Object, Object> colDescription;

    @FXML
    private TreeTableColumn<Object, Object> colItemCode;

    @FXML
    private TreeTableColumn<Object, Object> colOption;

    @FXML
    private TreeTableColumn<Object, Object> colQty;

    @FXML
    private TreeTableColumn<Object, Object> colUnitPrice;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private AnchorPane placeOrderPane;

    @FXML
    private JFXTreeTableView<CartTm> tblOrder;

    @FXML
    private JFXTextField txtCustomerName;

    @FXML
    private JFXTextField txtDescription;

    @FXML
    private JFXTextField txtQty;

    @FXML
    private JFXTextField txtSearch;

    ObservableList<CartTm> tmList = FXCollections.observableArrayList();

    public void backButtonOnAction(ActionEvent ignoredActionEvent) {
        Stage stage = (Stage) placeOrderPane.getScene().getWindow();
        try {
            stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../view/DashBoard.fxml")))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.show();
    }

    private double findTotal(){
        double total = 0;
        for (CartTm tm:tmList) {
            total += tm.getAmount();
        }
        return total;
    }

    @FXML
    void addToCartButtonOnAction(ActionEvent ignoredEvent) {
        boolean isExist = false;
        for (CartTm tm:tmList) {
            if (tm.getCode().equals(cmbItemCode.getValue())){
                tm.setQty(tm.getQty()+Integer.parseInt(txtQty.getText()));
                tm.setAmount(tm.getQty()*tm.getAmount());
                isExist = true;
            }
        }

        if (!isExist){
            JFXButton btn = new JFXButton("Delete");
            btn.setTextFill(Color.rgb(255,255,255));
            btn.setStyle("-fx-background-color: #e35c5c; -fx-font-weight: BOLD");



            CartTm cartTm = new CartTm(
                    cmbItemCode.getValue(),
                    txtDescription.getText(),
                    Double.parseDouble(lblUnitPrice.getText()),
                    Integer.parseInt(txtQty.getText()),
                    Double.parseDouble(lblUnitPrice.getText())*Integer.parseInt(txtQty.getText()),
                    btn
            );

            btn.setOnAction(actionEvent -> {
                tmList.remove(cartTm);
                lblTotal.setText(String.format("%.2f",findTotal()));
                tblOrder.refresh();
            });

            tmList.add(cartTm);

            TreeItem<CartTm> treeItem = new RecursiveTreeItem<>(tmList, RecursiveTreeObject::getChildren);
            tblOrder.setRoot(treeItem);
            tblOrder.setShowRoot(false);
        }
        lblTotal.setText(String.format("%.2f",findTotal()));
        tblOrder.refresh();
    }

    @FXML
    void clearButtonOnAction(ActionEvent ignoredEvent) {
        clearFields();
    }

    private void clearFields() {
        cmbCustomerId.setValue("");
        cmbItemCode.setValue("");
        txtCustomerName.clear();
        txtSearch.clear();
        txtDescription.clear();
        lblUnitPrice.setText("0.00");
        lblQtyOnHand.setText("");
        txtQty.clear();

    }

    @FXML
    void placeOrderButtonOnAction(ActionEvent ignoredEvent) throws SQLException, ClassNotFoundException {
        List<OrderDetails> detailsList = new ArrayList<>();

        for (CartTm tm:tmList) {
            detailsList.add(new OrderDetails(
                    lblOrderId.getText(),
                    tm.getCode(),
                    tm.getQty(),
                    tm.getUnitPrice()
            ));
        }

        Order order = new Order(
                lblOrderId.getText(),
                LocalDate.now().toString(),
                cmbCustomerId.getValue()
        );
        try {
            DBConnection.getInstance().getConnection().setAutoCommit(false);

            boolean orderPlaced = CrudUtil.execute(
                    "INSERT INTO orders VALUES(?,?,?)",
                    order.getId(),
                    Date.valueOf(order.getDate()),
                    order.getCustomerId()
            );

            boolean isOrderPlaced = true;
            if (orderPlaced) {
                for (OrderDetails detail:detailsList) {

                    boolean detailSaved = CrudUtil.execute(
                            "INSERT INTO orderdetail VALUES(?,?,?,?)",
                            detail.getOrderId(),
                            detail.getItemCode(),
                            detail.getQty(),
                            detail.getUnitPrice()
                    );

                    if (!detailSaved){
                        isOrderPlaced = false;
                    }

                }
            }else{
                isOrderPlaced = false;
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
                DBConnection.getInstance().getConnection().rollback();
            }

            if (isOrderPlaced){
                new Alert(Alert.AlertType.INFORMATION,"Order has been placed successfully!").show();
                DBConnection.getInstance().getConnection().commit();
                tmList.clear();
                tblOrder.refresh();
                clearFields();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
                DBConnection.getInstance().getConnection().rollback();
            }
        } catch (SQLException | ClassNotFoundException e) {
            DBConnection.getInstance().getConnection().rollback();
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().getConnection().setAutoCommit(true);
        }
    }


    @FXML
    void updateButtonOnAction(ActionEvent ignoredEvent) {
        Order order = new Order(
                lblOrderId.getText(),
                LocalDate.now().toString(),
                txtCustomerName.getText()
        );

        try {
            boolean isUpdate = CrudUtil.execute(
                    "UPDATE order SET date=? , customerId=? WHERE id=?",
                    order.getId(),
                    order.getDate(),
                    order.getCustomerId()
            );

            if (isUpdate){
                new Alert(Alert.AlertType.INFORMATION,"Item has been updated successfully!").show();
                tblOrder.refresh();
                clearFields();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colItemCode.setCellValueFactory(new TreeItemPropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new TreeItemPropertyValueFactory<>("desc"));
        colUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new TreeItemPropertyValueFactory<>("qty"));
        colAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("amount"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));

        generateId();
        loadCustomerId();
        loadItemCodes();

        cmbCustomerId.setOnAction(actionEvent -> setCustomerName());

        cmbItemCode.setOnAction(actionEvent -> setItemDetails());
    }

    private void setItemDetails() {
        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT * FROM item WHERE code=?",
                    cmbItemCode.getValue()
            );

            if (resultSet.next()){
                txtDescription.setText(resultSet.getString(2));
                lblUnitPrice.setText(String.format("%.2f",resultSet.getDouble(3)));
                lblQtyOnHand.setText(resultSet.getString(4));
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setCustomerName() {
        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT name FROM customer WHERE id=?",
                    cmbCustomerId.getValue()
            );

            if (resultSet.next()){
                txtCustomerName.setText(resultSet.getString(1));
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadItemCodes() {
        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT code FROM item"
            );

            ObservableList<String> itemCodes = FXCollections.observableArrayList();

            while (resultSet.next()){
                itemCodes.add(resultSet.getString(1));
            }

            cmbItemCode.setItems(itemCodes);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerId() {

        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT id FROM customer"
            );

            ObservableList<String> customerIds = FXCollections.observableArrayList();

            while (resultSet.next()){
                customerIds.add(resultSet.getString(1));
            }

            cmbCustomerId.setItems(customerIds);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void generateId() {
        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT id FROM orders ORDER BY id DESC LIMIT 1"
            );

            if (resultSet.next()){
                int num = Integer.parseInt(resultSet.getString(1).split("D")[1]);
                num++;
                lblOrderId.setText(String.format("D%03d",num));
            }else {
                lblOrderId.setText("D001");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
