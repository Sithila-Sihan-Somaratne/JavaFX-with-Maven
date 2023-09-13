package dto.tm;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class OrderTm extends RecursiveTreeObject<OrderTm> {
    private String id;
    private String date;
    private String custName;
    private JFXButton btn;
}
