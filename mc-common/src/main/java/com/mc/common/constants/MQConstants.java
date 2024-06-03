package com.mc.common.constants;

public class MQConstants {
    /**
     *  数据同步交换机
     */
    public static final String DATA_SYNC_EXCHANGE = "data-sync-exchange";

    /**
     *  music数据新增队列
     */
    public static final String MUSIC_DATA_INSERT_QUEUE = "music-data-insert-queue";

    /**
     *  dynamic数据新增队列
     */
    public static final String DYNAMIC_DATA_INSERT_QUEUE = "dynamic-data-insert-queue";

    /**
     *  music数据新增路由键
     */
    public static final String MUSIC_DATA_INSERT_KEY = "music:data:insert:key";

    /**
     *  dynamic数据新增路由键
     */
    public static final String DYNAMIC_DATA_INSERT_KEY = "dynamic:data:insert:key";


    /**
     *  music数据删除队列
     */
    public static final String MUSIC_DATA_DELETE_QUEUE = "music-data-delete-queue";

    /**
     *  dynamic数据删除队列
     */
    public static final String DYNAMIC_DATA_DELETE_QUEUE = "dynamic-data-delete-queue";

    /**
     *  music数据删除路由键
     */
    public static final String MUSIC_DATA_DELETE_KEY = "music:data:delete:key";

    /**
     *  dynamic数据删除路由键
     */
    public static final String DYNAMIC_DATA_DELETE_KEY = "dynamic:data:delete:key";


    /**
     *  评论数据交换机
     */
    public static final String COMMENT_SYNC_EXCHANGE = "comment-sync-exchange";

    /**
     *  评论数据队列
     */
    public static final String COMMENT_DATA_QUEUE = "comment-data-queue";

    /**
     *  评论数据路由键
     */
    public static final String COMMENT_DATA_KEY = "comment:data:key";

    /**
     *  点赞数据交换机
     */
    public static final String LIKE_SYNC_EXCHANGE = "like-sync-exchange";

    /**
     *  点赞数据队列
     */
    public static final String LIKE_DATA_QUEUE = "like-data-queue";

    /**
     *  点赞数据路由键
     */
    public static final String LIKE_DATA_KEY = "like:data:key";


}
