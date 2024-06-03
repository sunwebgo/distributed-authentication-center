package com.mc.common.entity.table;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 音乐表
 *
 * @author Xu huaiang
 * @TableName music
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Music implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 音乐id
     */
    private Long id;

    /**
     * 音乐名
     */
    private String name;

    /**
     * 音乐地址
     */
    private String musicUrl;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 作者
     */
    private String author;

    /**
     * 专辑
     */
    private String album;

    /**
     * 类型(1:流行,2:摇滚,3:民谣,4:电子,5:轻音乐)'
     */
    private Integer type;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;


}
