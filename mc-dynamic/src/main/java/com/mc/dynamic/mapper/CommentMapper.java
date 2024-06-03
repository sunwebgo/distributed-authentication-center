package com.mc.dynamic.mapper;


import com.mc.common.entity.table.Comment;
import com.mc.common.entity.vo.comment.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    Boolean commentIsExist(@Param("id") Long commentId);

    Boolean addComment(Comment comment);

    List<CommentVO> getComment(@Param("dynamicId") Long dynamicId,
                               @Param("start") Integer start,
                               @Param("size") Integer size);

    List<CommentVO> getChildren(@Param("rootId") Long rootId);

    Integer getCommentTotal(@Param("dynamicId") Long dynamicId,
                            @Param("rootComment") String rootComment);

    void deleteComment(@Param("id") Long commentId);

    List<Long> limitComment(@Param("byCommentUId") Long uId,
                               @Param("start") Integer start,
                               @Param("size") Integer size);

    String selectContent(@Param("id") Long respCommentId);

    Long getByCommentUId(@Param("id") Long commentId);
}




