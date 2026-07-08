package com.jonathanleite.vitrine.orderservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ClientServiceClientContractTest {

    @Test
    void shouldConfigureFeignClientForDockerDiscoveryAndOptionalLocalUrl() {
        FeignClient feignClient = ClientServiceClient.class.getAnnotation(FeignClient.class);

        assertThat(feignClient.name()).isEqualTo("client-api");
        assertThat(feignClient.path()).isEqualTo("/clients");
        assertThat(feignClient.url()).isEqualTo("${clients.service.url:}");
        assertThat(feignClient.fallbackFactory()).isEqualTo(ClientServiceFallbackFactory.class);
    }

    @Test
    void shouldSendUserIdHeaderWhenFetchingClient() throws NoSuchMethodException {
        Method method = ClientServiceClient.class.getMethod("getClientById", Long.class, String.class);
        RequestHeader requestHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);

        assertThat(method.getAnnotation(GetMapping.class).value()).containsExactly("/{id}");
        assertThat(requestHeader.value()).isEqualTo(ClientServiceClient.USER_ID_HEADER);
        assertThat(ClientServiceClient.SERVICE_USER_ID).isEqualTo("order-service");
    }
}
