package com.gaocui;

import com.gaocui.entity.Lead;
import com.gaocui.mapper.LeadMapper;
import com.gaocui.mapper.NotificationMapper;
import com.gaocui.mapper.ProductMapper;
import com.gaocui.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock private LeadMapper leadMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private ProductMapper productMapper;

    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadService(leadMapper, notificationMapper, productMapper);
    }

    @Test
    void getLeads_shouldFilterByMerchantId_only() {
        Lead lead1 = new Lead(); lead1.setId(1L); lead1.setMerchantId(1L); lead1.setBuyerEmail("a@b.com"); lead1.setBuyerMessage("msg");
        Lead lead2 = new Lead(); lead2.setId(2L); lead2.setMerchantId(2L); lead2.setBuyerEmail("c@d.com"); lead2.setBuyerMessage("msg2");

        // 只返回merchantId=1的
        when(leadMapper.selectList(any())).thenReturn(List.of(lead1));

        Map<String, Object> result = leadService.getLeads(1L, "all");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
        assertEquals("a@b.com", list.get(0).get("buyer_email"));
    }

    @Test
    void markContacted_shouldUpdateStatus() {
        Lead lead = new Lead(); lead.setId(1L); lead.setStatus("PENDING");
        when(leadMapper.selectById(1L)).thenReturn(lead);

        leadService.markContacted(1L);

        assertEquals("CONTACTED", lead.getStatus());
        verify(leadMapper).updateById(lead);
    }
}
