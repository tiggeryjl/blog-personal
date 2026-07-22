package com.blog.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {
    public static final String ACCOUNT_NOT_EMPTY = "账号不能为空";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_EXISTS = "账号已存在";
    public static final String ACCOUNT_DISABLED = "账号已被禁用";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ACCOUNT_LOGOUT = "账号已被注销";
    public static final String NOT_ADMIN = "您不是管理员，无法操作";
    public static final String NOT_ADMIN_ROLE = "当前账号未分配管理员角色，禁止登录";
    public static final String ONLY_ADMIN = "仅超级管理员可修改角色";
    public static final String NOT_UPDATE_ADMIN = "超级管理员不可被修改";
    public static final String NOT_SET_ADMIN = "仅可设置为普通用户或管理员，不能设置超级管理员";
    public static final String NOT_SET_ROLE = "不允许修改自己账号的角色";
    public static final String NOT_DELETE = "不可删除";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String LOGIN_EXPIRED_NOT_FOUND = "token已失效，请重新登录";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String ACCOUNT_PASSWORD_NOT_EMPTY = "账号和密码不能为空";

    public static final String NICKNAME = "用户_";
    public static final String USERNAME_RESTRICTION = "用户名只能3-16位字母、数字、下划线";
    public static final String USERNAME_NOT_PHONE = "用户名不能是11位手机号";
    public static final String USERNAME_NOT_EMAIL = "用户名不能是邮箱";
    public static final String USERNAME_EXISTS = "用户名已被注册";
    public static final String PHONE_FORMAT_ERROR = "手机号格式错误";
    public static final String PHONE_EXISTS = "手机号已被注册";
    public static final String EMAIL_FORMAT_ERROR = "邮箱格式错误";
    public static final String EMAIL_EXISTS = "邮箱已被注册";

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String PASSWORD_NOT_EMPTY = "密码不能为空";
    public static final String PASSWORD_LEN_SHORT = "密码长度需为6-20位";
    public static final String PASSWORD_TWO_NOT_EQUAL = "两次新密码输入不一致";
    public static final String PASSWORD_REPEAT = "新密码与旧密码一致";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";

    public static final String TOKEN_EXPIRED = "未登录或Token已过期，请重新登录";
    public static final String TOKEN_NOT_SUPPORTED = "不支持的token";
    public static final String TOKEN_FORMAT_ERROR = "token格式错误";
    public static final String TOKEN_SIGNATURE_ERROR = "签名错误，无效Token";
    public static final String TOKEN_EMPTY = "token为空";

    public static final String CODE_VERIFICATION_FAILED = "验证码校验失败";

    public static final String ASSOCIATED_CATEGORY_ARTICLES = "当前分类已关联文章，无法删除";
    public static final String ASSOCIATED_TAG_ARTICLES = "当前标签已关联文章，无法删除";
}
