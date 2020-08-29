package com.yumingzhu.shirodemo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: lijincan
 * @date: 2020年02月26日 16:13
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = -6100906301506454259L;


    /**
     * test1
     */

    private int id;

    private String name;

    private String pwd;

    private String permission;

}
