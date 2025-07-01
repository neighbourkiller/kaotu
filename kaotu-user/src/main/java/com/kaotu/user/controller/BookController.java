package com.kaotu.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/book")
@Tag(name = "BookController", description = "图书相关接口")
public class BookController {
}
