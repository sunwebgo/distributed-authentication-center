package com.mc.dynamic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.MQConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.dto.dynamic.CommentDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Comment;
import com.mc.common.entity.vo.comment.CommentVO;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.Http;
import com.mc.common.utils.BeanCopyUtils;
import com.mc.common.utils.RedisUtil;
import com.mc.common.utils.SnowFlakeUtil;
import com.mc.dynamic.mapper.CommentMapper;
import com.mc.dynamic.mapper.DynamicMapper;
import com.mc.dynamic.service.CommentService;
import com.mc.dynamic.service.DynamicService;
import com.mc.dynamic.utils.SensitiveReplaceUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @DubboReference(timeout = 2000)
    private UserServiceInterface userServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private SensitiveReplaceUtil sensitiveReplaceUtil;

    @Resource
    private DynamicService dynamicService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private DynamicMapper dynamicMapper;

    /**
     * 添加评论
     *
     * @param commentDTO
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult addComment(CommentDTO commentDTO) {
        Comment comment = new Comment();
        // 判断评论者是否存在
        if (ObjectUtils.isNotEmpty(commentDTO.getUId())) {
            userServiceInterface.userIsExist(commentDTO.getUId()).whenCompleteAsync((result, throwable) -> {
                if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                    throw new RuntimeException(result.getMessage());
                }
            }, threadPoolExecutor);
        }
        comment.setUId(commentDTO.getUId());
        comment.setUName(commentDTO.getUName());
        comment.setUAvatarUrl(commentDTO.getUAvatarUrl());
        comment.setByCommentUId(commentDTO.getByCommentUId());
        // 判断评论对象-动态是否存在
        if (!dynamicService.dynamicIsExist(commentDTO.getCommentObjId())) {
            return ResponseResult.error(Http.DYNAMIC_NOT_EXIST.getMessage());
        }
        comment.setCommentObjId(commentDTO.getCommentObjId());
        // 敏感词过滤
        String content = sensitiveReplaceUtil.replaceSensitiveWord(commentDTO.getContent(), SensitiveReplaceUtil.minMatchType);
        comment.setContent(content);
        // 判断当前评论是否有根评论
        if (ObjectUtils.isNotEmpty(commentDTO.getRootId())) {
            // 判断根评论是否存在
            if (commentMapper.commentIsExist(commentDTO.getRootId())) {
                comment.setRootId(commentDTO.getRootId());
            }
        }
        // 判断当前评论是否有被回复的评论
        if (ObjectUtils.isNotEmpty(commentDTO.getRespCommentId())) {
            // 判断被回复的评论是否存在
            if (commentMapper.commentIsExist(commentDTO.getRespCommentId())) {
                comment.setRespCommentId(commentDTO.getRespCommentId());
            }
        }
        // 雪花算法生成id
        comment.setId(SnowFlakeUtil.getNextId());
        // 保存评论
        Boolean addResult = commentMapper.addComment(comment);
        if (!addResult) {
            return ResponseResult.error(Http.COMMENT_FAIL.getMessage());
        }
        CommentVO commentVO = BeanCopyUtils.copyBean(comment, CommentVO.class);
        commentVO.setCommentDate(new Date());
        // 将评论写入到消息队列
        rabbitTemplate.convertAndSend(
                MQConstants.COMMENT_SYNC_EXCHANGE,
                MQConstants.COMMENT_DATA_KEY,
                commentVO);
        return ResponseResult.success(commentVO);
    }

    /**
     * 查询动态评论
     *
     * @param page
     * @param size
     * @param dynamicId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @Override
    public ResponseResult<PageVO> getComment(Integer page, Integer size, Long dynamicId) {
        Integer start = (page - 1) * size;
        PageVO pageVO = new PageVO();
        // 判断动态是否存在
        Boolean exist = dynamicService.dynamicIsExist(dynamicId);
        if (!exist) {
            return ResponseResult.error(Http.DYNAMIC_NOT_EXIST.getMessage());
        }
        // 查询动态的评论总数
        Integer commentTotal = commentMapper.getCommentTotal(dynamicId, null);
        pageVO.setTotal(commentTotal);
        // 查询动态的根评论总数
        Integer rootTotal = commentMapper.getCommentTotal(dynamicId, CommonConstants.ROOT_COMMENT);
        pageVO.setTotalPage(rootTotal % size == 0 ? rootTotal / size : rootTotal / size + 1);
        // 查询动态的根评论
        List<CommentVO> rootComment = commentMapper.getComment(dynamicId, start, size);
        if (rootComment.isEmpty()) { // 动态没有评论
            return ResponseResult.success();
        }
        // 查询根评论的子评论
        rootComment.stream().forEach(comment -> {
            List<CommentVO> children = getChildren(comment.getId());
            if (!children.isEmpty()) {
                comment.setChildren(children);
            }
        });
        pageVO.setList(rootComment);
        return ResponseResult.success(pageVO);
    }

    /**
     * 查询对应的子评论
     *
     * @param rootId
     * @return {@link List<CommentVO>}
     */
    private List<CommentVO> getChildren(Long rootId) {
        // 查询根评论的子评论
        List<CommentVO> children = commentMapper.getChildren(rootId);
        if (CollectionUtils.isEmpty(children)) {
            return children;
        }
        return children;
    }

    /**
     * 删除评论
     *
     * @param uId
     * @param commentId
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult deleteComment(Long uId, Long commentId) {
        // 判断评论是否存在
        Long byCommentUId = commentMapper.getByCommentUId(commentId);
        if (ObjectUtils.isEmpty(byCommentUId)) {
            return ResponseResult.error(Http.COMMENT_NOT_BELONG.getMessage());
        }
        // 删除评论（同时删除该评论的子评论/回复的评论）
        commentMapper.deleteComment(commentId);
        // 删除评论缓存
        RedisUtil.hashDelete(CacheConstants.COMMENT_RESP + byCommentUId, commentId.toString());
        return ResponseResult.success();
    }

    /**
     * 查询用户的被评论列表
     *
     * @param page
     * @param size
     * @param uId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @Override
    public ResponseResult<PageVO> byCommentList(Integer page, Integer size, Long uId) {
        Integer start = (page - 1) * size;
        PageVO pageVO = new PageVO();
        // 查询用户的被评论总数
        pageVO.setTotal(RedisUtil.hashSize(CacheConstants.COMMENT_RESP + uId).intValue());
        pageVO.setTotalPage(pageVO.getTotal() % size == 0 ? pageVO.getTotal() / size : pageVO.getTotal() / size + 1);
        // 查询用户的被评论列表
        List<Long> commentIdList = commentMapper.limitComment(uId, start, size);
        if (CollectionUtils.isEmpty(commentIdList)) {
            return ResponseResult.success();
        }
        List<CommentVO> commentVOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        commentIdList.stream().forEach(commentId -> {
            Object obj = RedisUtil.hashGet(CacheConstants.COMMENT_RESP + uId, commentId.toString());
            if (ObjectUtils.isNotEmpty(obj)) {
                try {
                    CommentVO commentVO = objectMapper.readValue(obj.toString(), CommentVO.class);
                    String byRespContent;
                    // 判断被回复的评论id是否为空，如果为空则查询动态内容，不为空则查询被回复的评论内容
                    if (ObjectUtils.isEmpty(commentVO.getRespCommentId())) {
                        byRespContent = dynamicMapper.selectContent(commentVO.getCommentObjId());
                    } else {
                        byRespContent = commentMapper.selectContent(commentVO.getRespCommentId());
                    }
                    commentVO.setByRespContent(byRespContent);
                    commentVOList.add(commentVO);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        pageVO.setList(commentVOList);
        return ResponseResult.success(pageVO);
    }

}
