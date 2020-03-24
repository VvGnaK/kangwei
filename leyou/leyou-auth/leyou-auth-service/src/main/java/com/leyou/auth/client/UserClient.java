package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author VvGnaK
 * @date 2020-03-14 16:21
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {

}
