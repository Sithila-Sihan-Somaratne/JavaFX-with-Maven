package dto.tm;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ItemTm extends RecursiveTreeObject<ItemTm> {
    private String code;
    private String description;
    private double unitPrice;
    private int qtyOnHand;
    private JFXButton btn;

}
