package dto.tm;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class CartTm extends RecursiveTreeObject<CartTm> {
    private String code;
    private String desc;
    private double unitPrice;
    private int qty;
    private double amount;
    private JFXButton btn;


}
