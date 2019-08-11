package com.personal.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 链家房源信息 映射vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseInfoLJ implements Serializable {

    private String infoUrl;
    private String houseNum;

}
