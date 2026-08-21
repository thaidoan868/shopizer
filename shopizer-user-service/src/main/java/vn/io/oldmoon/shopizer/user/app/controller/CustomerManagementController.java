package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;

@RestController
@RequestMapping("/api/v1/users/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management Endpoints")
public class CustomerManagementController extends AbstractController {}
