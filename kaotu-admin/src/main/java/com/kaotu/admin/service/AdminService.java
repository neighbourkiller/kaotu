package com.kaotu.admin.service;

import com.kaotu.admin.model.dto.CommentByBookIdDto;
import com.kaotu.admin.model.dto.CommentStatus;
import com.kaotu.admin.model.dto.SearchPageDto;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.dto.UserUpdateDto;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CommentVO;
import com.kaotu.base.result.PageResult;

public interface AdminService {
    String login(Admin admin);
    PageResult<User> listUsers(PageParams pageParams);
    void updateUser(UserUpdateDto userUpdateDto) throws Exception;
    void deleteUser(String userId);
    PageResult<BookVO> listBooks(PageParams pageParams);
    void modifyBook(Book book);
    void deleteBook(Integer bookId);
    PageResult<CommentVO> commentsPage(SearchPageDto searchPageDto);

    PageResult<CommentVO> commentsPageByBookId(CommentByBookIdDto commentByBookIdDto);

    void updateCommentStatus(CommentStatus commentStatus);
}
