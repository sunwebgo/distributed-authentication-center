package com.mc.common.entity.elastic;

import com.mc.common.enums.DataChangType;
import com.mc.common.enums.Table;
import lombok.*;

import java.io.Serializable;

/**
 * 数据改变信息
 *
 * @author Xu huaiang
 * @date 2024/03/11
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DataChangInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     *  涉及的表
     */
    private Table tableName;

    /**
     * 数据改变类型
     */
    private DataChangType dataChangType;

    /**
     * 改变的数据（insert:新增单条或者多条数据
     *          update:修改单条数据
     *          delete:删除单条或者多条数据）
     */
    private T changedData;
}
