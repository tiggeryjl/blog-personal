package com.blog.aspect;

import com.blog.annotation.OperLog;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.OperLogConstant;
import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.entity.SysOperLog;
import com.blog.pojo.entity.SysUser;
import com.blog.service.OperLogService;
import com.blog.utils.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 后台操作日志切面
 * 只记录新增、修改、删除三类请求，查询请求不记录
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    /**
     * 不记录日志的接口：登录相关
     */
    private static final Set<String> SKIP_URLS = Set.of(
            "/admin/admin/login",
            "/admin/admin/refreshToken",
            "/admin/admin/logout"
    );

    /**
     * 不记录日志的路径前缀：操作日志自身，避免递归
     */
    private static final String SKIP_PREFIX = "/admin/operlog";

    /**
     * 需要脱敏的参数名关键字
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "\"([A-Za-z0-9_]*(?:password|pwd|token|emailCode|secret|accessKey)[A-Za-z0-9_]*)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private OperLogService operLogService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Pointcut("execution(* com.blog.controller.admin..*(..))")
    public void adminControllerPointcut() {
    }

    @Around("adminControllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            try {
                saveLog(joinPoint, result, error, System.currentTimeMillis() - start);
            } catch (Exception e) {
                // 日志记录失败不影响业务
                log.warn("操作日志记录失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 组装并保存日志
     */
    private void saveLog(ProceedingJoinPoint joinPoint, Object result, Throwable error, long costTime) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        // 只记录新增、修改、删除，查询不记录
        if (!"POST".equalsIgnoreCase(method)
                && !"PUT".equalsIgnoreCase(method)
                && !"DELETE".equalsIgnoreCase(method)) {
            return;
        }

        String url = request.getRequestURI();
        if (SKIP_URLS.contains(url) || url.startsWith(SKIP_PREFIX)) {
            return;
        }

        // 必须在当前线程取用户ID，异步线程拿不到 ThreadLocal
        Long userId = BaseContext.getCurrentId();
        String username = null;
        String nickname = null;
        if (userId != null) {
            SysUser user = sysUserMapper.getByUserId(userId);
            if (user != null) {
                username = user.getUsername();
                nickname = user.getNickname();
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        OperLog annotation = signature.getMethod().getAnnotation(OperLog.class);

        String module = annotation != null && !annotation.module().isEmpty()
                ? annotation.module()
                : resolveModule(joinPoint.getTarget().getClass().getSimpleName());
        int operType = annotation != null && annotation.type() != OperLogConstant.TYPE_AUTO
                ? annotation.type()
                : resolveType(methodName, method);
        String title = annotation != null && !annotation.title().isEmpty()
                ? annotation.title()
                : resolveTitle(operType);

        String userAgent = request.getHeader("User-Agent");
        String param = maskSensitive(serialize(filterArgs(joinPoint.getArgs())));

        SysOperLog operLog = SysOperLog.builder()
                .operUserId(userId)
                .operUsername(username)
                .operNickname(nickname)
                .moduleName(module)
                .operTitle(title)
                .operType(operType)
                .requestUrl(truncate(url, OperLogConstant.MAX_URL_LENGTH))
                .requestMethod(method)
                .ipAddress(IpUtil.getClientIp(request))
                .browser(parseBrowser(userAgent))
                .os(parseOs(userAgent))
                .location(null)
                .operParam(param)
                .operResult(truncate(maskSensitive(serializeResult(result)),
                        OperLogConstant.MAX_TEXT_LENGTH))
                .costTime(costTime)
                .status(error == null ? StatusConstant.ENABLE : StatusConstant.DISABLE)
                .errorMsg(error == null ? null
                        : truncate(error.getMessage(), OperLogConstant.MAX_TEXT_LENGTH))
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .build();
        operLogService.save(operLog);
    }

    /**
     * 过滤掉无法序列化的参数
     */
    private List<Object> filterArgs(Object[] args) {
        List<Object> result = new ArrayList<>();
        if (args == null) {
            return result;
        }
        for (Object arg : args) {
            if (arg == null) {
                result.add(null);
                continue;
            }
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]) {
                continue;
            }
            result.add(arg);
        }
        return result;
    }

    /**
     * 序列化返回值，二进制内容不记录
     */
    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof byte[] || result instanceof HttpServletResponse) {
            return "[二进制内容]";
        }
        return serialize(result);
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /**
     * 敏感字段脱敏
     */
    private String maskSensitive(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SENSITIVE_PATTERN.matcher(json).replaceAll("\"$1\":\"***\"");
    }

    private String resolveModule(String controllerName) {
        String base = controllerName.replace("Controller", "");
        switch (base) {
            case "Article":
                return "文章管理";
            case "Category":
                return "分类管理";
            case "Tag":
                return "标签管理";
            case "Comment":
                return "评论管理";
            case "Daily":
                return "日常管理";
            case "Home":
                return "首页统计";
            case "Link":
                return "友链管理";
            case "Notice":
                return "通知中心";
            case "Report":
                return "举报管理";
            case "SysAdmin":
                return "用户管理";
            case "SysMenu":
                return "权限管理";
            case "SysRole":
                return "角色管理";
            case "OperLog":
                return "操作日志";
            default:
                return base + "管理";
        }
    }

    /**
     * 根据方法名推断操作类型
     */
    private int resolveType(String methodName, String httpMethod) {
        String name = methodName.toLowerCase();
        if (name.contains("export")) {
            return OperLogConstant.TYPE_EXPORT;
        }
        if (name.contains("import")) {
            return OperLogConstant.TYPE_IMPORT;
        }
        if (name.contains("delete") || name.contains("remove") || name.contains("clear")) {
            return OperLogConstant.TYPE_DELETE;
        }
        if (name.contains("add") || name.contains("save") || name.contains("create")
                || name.contains("apply")) {
            return OperLogConstant.TYPE_ADD;
        }
        if (name.contains("update") || name.contains("edit") || name.contains("audit")
                || name.contains("change") || name.contains("status") || name.contains("reset")) {
            return OperLogConstant.TYPE_UPDATE;
        }
        if ("POST".equalsIgnoreCase(httpMethod)) {
            return OperLogConstant.TYPE_ADD;
        }
        if ("PUT".equalsIgnoreCase(httpMethod)) {
            return OperLogConstant.TYPE_UPDATE;
        }
        if ("DELETE".equalsIgnoreCase(httpMethod)) {
            return OperLogConstant.TYPE_DELETE;
        }
        return OperLogConstant.TYPE_QUERY;
    }

    private String resolveTitle(int operType) {
        switch (operType) {
            case OperLogConstant.TYPE_ADD:
                return "新增";
            case OperLogConstant.TYPE_UPDATE:
                return "修改";
            case OperLogConstant.TYPE_DELETE:
                return "删除";
            case OperLogConstant.TYPE_EXPORT:
                return "导出";
            case OperLogConstant.TYPE_IMPORT:
                return "导入";
            default:
                return "查询";
        }
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (userAgent.contains("Edg")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari")) {
            return "Safari";
        }
        return "其他";
    }

    private String parseOs(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "其他";
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
