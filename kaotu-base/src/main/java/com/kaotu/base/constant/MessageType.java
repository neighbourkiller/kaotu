package com.kaotu.base.constant;

public enum MessageType {
    // 互动社交
    POST_LIKE,          // 帖子点赞
    POST_COLLECT,       // 帖子收藏
    NEW_COMMENT,        // 新评论
    REPLY_COMMENT,      // 评论回复
    NEW_FOLLOWER,       // 新关注

    // 系统管理
    SYSTEM_ANNOUNCEMENT,   // 系统公告
    WELCOME_MESSAGE,        // 欢迎消息
    CONTENT_VIOLATION,      // 内容违规
    ACCOUNT_SUSPENSION,     // 账号封禁

    // 个人消息
    INFO_UPDATE,          // 信息更新
    OPERATION_SUCCESS,   // 操作成功
    OPERATION_FAILURE,  // 操作失败

    // 功能特色
//    ANSWER_ACCEPTED, // 答案被采纳
    DAILY_CHECK_IN_REMINDER // 每日签到提醒
}