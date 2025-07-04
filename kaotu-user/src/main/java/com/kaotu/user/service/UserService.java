package com.kaotu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaotu.base.model.dto.CommentDto;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.CommentVO;

import java.util.List;


public interface UserService extends IService<User> {

    String register(User user);
    String login(User user);
    void modifyEmail(String userId, String email);
    void recordBrowseTime(int bookId,int timeInSeconds);
    void commentBook(CommentDto commentDto);
    List<CommentVO> myComments();
    void upvoteComment(Integer commentId);
}
