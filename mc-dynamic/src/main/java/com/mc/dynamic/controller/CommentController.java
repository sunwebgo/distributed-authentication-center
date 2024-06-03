package com.mc.dynamic.controller;

import com.mc.common.constants.CommonConstants;
import com.mc.common.entity.dto.dynamic.CommentDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.Http;
import com.mc.dynamic.service.CommentService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 添加评论
     *
     * @param commentDTO
     * @return {@link ResponseResult}
     */
    @PostMapping("/add")
    public ResponseResult addComment(@Valid @RequestBody CommentDTO commentDTO) {
        return commentService.addComment(commentDTO);
    }

    /**
     * 分页查询动态评论
     *
     * @param page
     * @param size
     * @param dynamicId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @GetMapping("/list")
    public ResponseResult<PageVO> getComment(@RequestParam(value = "page", required = false) Integer page,
                                             @RequestParam(value = "size", required = false) Integer size,
                                             @RequestParam Long dynamicId) {
        page = page == null || page <= 0 ? CommonConstants.DEFAULT_PAGE : page;
        size = size == null || size <= 0 ? CommonConstants.CUSTOMER_SIZE : size;
        if (ObjectUtils.isEmpty(dynamicId)) {
            return ResponseResult.error(Http.DYNAMIC_NOT_EXIST.getMessage());
        }
        return commentService.getComment(page, size, dynamicId);
    }

    /**
     * 用户删除评论
     *
     * @return {@link ResponseResult}
     */
    @DeleteMapping("/delete")
    public ResponseResult deleteComment(@RequestParam(value = "uId", required = false) Long uId,
                                        @RequestParam(value = "commentId", required = false) Long commentId) {
        if (ObjectUtils.isEmpty(uId)) {
            return ResponseResult.error(Http.USER_NOT_EXIST.getMessage());
        }
        if (ObjectUtils.isEmpty(commentId)) {
            return ResponseResult.error(Http.COMMENT_NOT_EXIST.getMessage());
        }
        return commentService.deleteComment(uId, commentId);
    }

    /**
     * 查询用户的被评论列表
     * @param page
     * @param size
     * @param uId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @GetMapping("/list/by-comment")
    public ResponseResult<PageVO> byCommentList(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "size", required = false) Integer size,
                                                   @RequestParam(value = "uId",required = false) Long uId) {
        page = page == null || page <= 0 ? CommonConstants.DEFAULT_PAGE : page;
        size = size == null || size <= 0 ? CommonConstants.CUSTOMER_SIZE : size;
        if (ObjectUtils.isEmpty(uId)) {
            return ResponseResult.error(Http.USER_NOT_EXIST.getMessage());
        }
        return commentService.byCommentList(page, size, uId);
    }
}
