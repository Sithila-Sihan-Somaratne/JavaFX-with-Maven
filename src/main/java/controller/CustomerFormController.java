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
import entity.Customer;
import dto.tm.CustomerTm;
import util.CrudUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CustomerFormController implements Initializable {
    public Label lblCustId;
    @FXML
    private TreeTableColumn<Object, Object> colAddress;

    @FXML
    private TreeTableColumn<Object, Object> colCustId;

    @FXML
    private TreeTableColumn<Object, Object> colCustName;

    @FXML
    private TreeTableColumn<Object, Object> colSalary;

    @FXML
    private TreeTableColumn<Object, Object> colOption;

    @FXML
    private AnchorPane customerPane;

    @FXML
    private JFXTreeTableView<CustomerTm> tblCustomer;

    @FXML
    private JFXTextField txtAddress;

    @FXML
    private JFXTextField txtName;

    @FXML
    private JFXTextField txtSalary;

    @FXML
    private JFXTextField txtSearch;

    public void backButtonOnAction(ActionEvent ignoredActionEvent) {
        Stage stage = (Stage) customerPane.getScene().getWindow();
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
        txtName.clear();
        txtAddress.clear();
        txtSalary.clear();
        txtSearch.clear();
        tblCustomer.refresh();
    }

    @FXML
    void saveButtonOnAction(ActionEvent ignoredEvent) {
        Customer customer = new Customer(
                lblCustId.getText(),
                txtName.getText(),
                txtAddress.getText(),
                Double.parseDouble(txtSalary.getText())
        );
        try {
            boolean isSaved = CrudUtil.execute(
                    "INSERT INTO customer VALUES(?,?,?,?)",
                    customer.getId(),
                    customer.getName(),
                    customer.getAddress(),
                    customer.getSalary()
            );

            if (isSaved) {
                new Alert(Alert.AlertType.INFORMATION,"Customer has been saved successfully!").show();
                loadTable();
                clearFields();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateButtonOnAction(ActionEvent ignoredEvent) {
        Customer customer = new Customer(
                lblCustId.getText(),
                txtName.getText(),
                txtAddress.getText(),
                Double.parseDouble(txtSalary.getText())
        );
        try {

            boolean isUpdate = CrudUtil.execute(
                    "UPDATE customer SET name=? , address=?, salary=? WHERE id=?",
                    customer.getName(),
                    customer.getAddress(),
                    customer.getSalary(),
                    customer.getId()
            );

            if (isUpdate){
                new Alert(Alert.AlertType.INFORMATION,"Customer has been updated successfully!").show();
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
        colCustId.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
        colCustName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new TreeItemPropertyValueFactory<>("address"));
        colSalary.setCellValueFactory(new TreeItemPropertyValueFactory<>("salary"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));
        generateId();
        loadTable();

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->{
            if (newValue!=null){
                setData(newValue);
            }
        });

        txtSearch.textProperty().addListener((observableValue, oldValue, newValue) -> tblCustomer.setPredicate(customerTmTreeItem -> customerTmTreeItem.getValue().getId().contains(newValue) ||
                customerTmTreeItem.getValue().getName().contains(newValue)));
    }

    private void setData(TreeItem<CustomerTm> value) {
        lblCustId.setText(value.getValue().getId());
        txtName.setText(value.getValue().getName());
        txtAddress.setText(value.getValue().getAddress());
        txtSalary.setText(String.valueOf(value.getValue().getSalary()));
    }

    private void loadTable() {
        ObservableList<CustomerTm> tmList = FXCollections.observableArrayList();
        try {
            List<Customer> list = new ArrayList<>();

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT * FROM customer"
            );

            while (resultSet.next()) {
                list.add(new Customer(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getDouble(4)
                ));
            }

            for (Customer customer:list) {
                JFXButton btn = new JFXButton("Delete");
                btn.setStyle("-fx-background-color: #e35c5c; -fx-font-weight: BOLD");
                btn.setTextFill(Color.rgb(255,255,255));
                btn.setOnAction(actionEvent -> {
                    try {
                        Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to delete " + customer.getId() + " customer ? ", ButtonType.YES, ButtonType.NO).showAndWait();
                        if (buttonType.get() == ButtonType.YES){
                            boolean isDelete = CrudUtil.execute(
                                    "DELETE FROM customer WHERE id=?",
                                    customer.getId()
                            );
                            if (isDelete){
                                new Alert(Alert.AlertType.INFORMATION,"Customer has been deleted successfully!").show();
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

                tmList.add(new CustomerTm(
                        customer.getId(),
                        customer.getName(),
                        customer.getAddress(),
                        customer.getSalary(),
                        btn
                ));
            }

            TreeItem<CustomerTm> treeItem = new RecursiveTreeItem<>(tmList, RecursiveTreeObject::getChildren);
            tblCustomer.setRoot(treeItem);
            tblCustomer.setShowRoot(false);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateId() {
        try {

            ResultSet resultSet = CrudUtil.execute(
                    "SELECT id FROM customer ORDER BY id DESC LIMIT 1"
            );

            if (resultSet.next()){
                int num = Integer.parseInt(resultSet.getString(1).split("C")[1]);
                num++;
                lblCustId.setText(String.format("C%03d",num));
            }else {
                lblCustId.setText("C001");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
