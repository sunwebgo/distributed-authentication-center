package com.mc.dynamic.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CollectMapper {
    Boolean collect(@Param("uId") Long uId,
                    @Param("type") Integer type,
                    @Param("objectId") Long objectId);

    Boolean cancelCollect(@Param("uId") Long uId,
                          @Param("type") Integer type,
                          @Param("objectId") Long objectId);

    List<Long> collectList(@Param("start") int start, @Param("size") Integer size, @Param("uId") Long uId, @Param("type") Integer type);

    Integer collectCount(@Param("uId") Long uId, @Param("type") Integer type);
}
