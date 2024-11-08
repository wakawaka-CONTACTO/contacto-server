package org.kiru.user.auth.argumentresolve;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isParamHasUserIdAnnotation = parameter.hasParameterAnnotation(UserId.class);
        boolean isParamLongType = Long.class.equals(parameter.getParameterType());
        return isParamHasUserIdAnnotation && isParamLongType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String userIdHeader = webRequest.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user ID format in X-User-Id header");
            }
        } else {
            throw new IllegalArgumentException("X-User-Id header is missing");
        }
    }
}
