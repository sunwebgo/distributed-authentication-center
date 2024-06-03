package com.mc.dynamic.mapper;


import com.mc.common.entity.table.Dynamic;
import com.mc.common.entity.to.dynamic.DynamicCacheDataTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DynamicMapper {
    List<Dynamic> getAllDynamic();

    Integer dynamicCount(@Param("uId") Long selfDynamic);

    List<Dynamic> dynamicMapper(@Param("start") int start,
                                @Param("size") Integer size,
                                @Param("uId") Long selfDynamic);

    List<DynamicCacheDataTO> getDynamicCacheData(@Param("id") Long id);

    void updateLikeCount(@Param("dId") Long id, @Param("likeCount") int i);

    List<Dynamic> getDynamicByIds(@Param("ids") List<Long> ids);

    Boolean addDynamic(@Param("id") Long id,
                       @Param("uId") Long uId,
                       @Param("uName") String uName,
                       @Param("avatarUrl") String avatarUrl,
                       @Param("content") String content,
                       @Param("imgUrls") String imgUrls,
                       @Param("musicId") Long musicId,
                       @Param("musicName") String musicName,
                       @Param("musicAuthor") String musicAuthor,
                       @Param("musicCoverUrl") String musicCoverUrl,
                       @Param("musicUrl") String musicUrl);

    Boolean dynamicIsExist(@Param("id") Long dynamicId);

    Boolean deleteDynamic(@Param("uId") Long uId,
                          @Param("dynamicId") Long dynamicId);

    Long getDynamicUserInfo(@Param("id") Long commentObjId);

    String selectContent(@Param("id") Long commentObjId);
}
