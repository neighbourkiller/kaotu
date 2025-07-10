# README

#### 1.kaotu-admin

管理员模块：对用户信息、书籍信息、评论信息的删改查

#### 2.kaotu-base

`constant`--定义的静态常量

`context`--`UserContext.java` --用户线程，存储解析token后的用户id

`exception`--异常处理

`handler`--`MyMetaObjectHandler.java`--MyBatis-Plus自动填充处理

`model`--实体类

`properties`--配置类，用于读取`application.yml`中的配置

`result`--返回结果类

`utils`--包含加解密工具、日志工具

#### 3.kaotu-gateway

网关模块，用于配置请求路由，在`application.yml`中配置其他模块的运行IP+端口

#### 4.kaotu-parant

`pom.xml`--用于项目依赖的版本管理

#### 5.kaotu-user

用户模块，包括用户能使用功能。

`UserController`--用户基本功能接口，登录，修改个人信息，收藏书籍，发出评论...

`BookController`--书籍基本功能接口，查看书籍详情页，查看书籍评论...

`PostController`--论坛帖子相关接口

