package com.itmk.config.security.handler;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.itmk.utils.ResultVo;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 自定义无权限访问
 * 目的：返回JSON格式数据
 */
@Component("customAccessDeineHandler")
public class CustomAccessDeineHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        String res =  JSONObject.toJSONString(new ResultVo("无权限访问,请联系管理员!",700,null), SerializerFeature.DisableCircularReferenceDetect);
        //设置返回格式
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = httpServletResponse.getOutputStream();
        out.write(res.getBytes("UTF-8"));
        out.flush();
        out.close();
    }
}