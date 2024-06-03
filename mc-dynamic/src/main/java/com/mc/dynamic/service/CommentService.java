package com.mc.dynamic.service;

import com.mc.common.entity.dto.dynamic.CommentDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.vo.page.PageVO;

public interface CommentService {
    ResponseResult addComment(CommentDTO commentDTO);

    ResponseResult<PageVO> getComment(Integer page, Integer size, Long dynamicId);

    ResponseResult deleteComment(Long uId, Long commentId);

    ResponseResult<PageVO> byCommentList(Integer page, Integer size, Long uId);
}
