package com.mc.common.entity.vo.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer total;

    private Integer totalPage;

    private List<T> list;
}
