package vn.io.oldmoon.shopizer.user.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import vn.io.oldmoon.shopizer.user.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.facade.CustomerFacade;
import vn.io.oldmoon.shopizer.user.app.facade.UserFacade;
import vn.io.oldmoon.shopizer.user.bussiness.exception.ErrorCode;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest
@Import(SecurityConfig.class) // <-- your class name
class CustomerManagementControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomerFacade customerFacade;

    @MockitoBean
    UserFacade userFacade;

    @Test
    void shouldReturnProperErrorResponse() throws Exception {
        // given
        PersistableCustomer invalidCustomer = new PersistableCustomer();
        invalidCustomer.setEmail("invalid");
        invalidCustomer.setPassword("short");

        // when
        var request = MockMvcRequestBuilders.post("/api/v1/users/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomer));

        // then
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().is(ErrorCode.VALIDATION_FAILED.getHttpStatus().value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.getError()))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }
}