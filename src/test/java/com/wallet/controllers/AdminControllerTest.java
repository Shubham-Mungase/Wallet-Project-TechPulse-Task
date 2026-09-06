package com.wallet.controllers;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.repository.UserRepository;
import com.wallet.restcontroller.AdminController;
import com.wallet.security.JwtService;
import com.wallet.service.AdminService;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
  

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

  

    @Test
    void getAllWallets_shouldReturn200() throws Exception {

        Page<WalletResponsese> page =
                new PageImpl<>(Collections.emptyList());

        when(adminService.getAllWallets(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/admin/wallets")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());
    }

    @Test
    void getAllTransactions_shouldReturn200() throws Exception {

        Page<TransactionResponse> page =
                new PageImpl<>(Collections.emptyList());

        when(adminService.getAllTransactions(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/admin/transactions")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());
    }
}