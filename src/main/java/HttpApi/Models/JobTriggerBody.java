package HttpApi.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update by andrey on 00:03 02.03.2021
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobTriggerBody {
    private String callBackCannelId;
    private String tags;
}
