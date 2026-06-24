package com.gaocui;

import com.gaocui.common.BizException;
import com.gaocui.dto.PublishProductDTO;
import com.gaocui.mapper.ProductImageMapper;
import com.gaocui.mapper.ProductMapper;
import com.gaocui.mapper.ProductTagMapper;
import com.gaocui.service.ProductSearchService;
import com.gaocui.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductMapper productMapper;
    @Mock private ProductImageMapper imageMapper;
    @Mock private ProductTagMapper tagMapper;
    @Mock private ProductSearchService searchService;
    @Mock private ObjectProvider<ProductSearchService> searchServiceProvider;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        when(searchServiceProvider.getIfAvailable()).thenReturn(searchService);
        productService = new ProductService(productMapper, imageMapper, tagMapper, searchServiceProvider);
    }

    @Test
    void freeMerchant_shouldRejectOver2Products() {
        when(productMapper.selectCount(any())).thenReturn(2L);

        PublishProductDTO dto = new PublishProductDTO();
        dto.setTitle("测试"); dto.setPrice(BigDecimal.TEN);

        assertThrows(BizException.class, () ->
            productService.publish(1L, false, dto)
        );
    }

    @Test
    void vipMerchant_shouldRejectOver100Products() {
        when(productMapper.selectCount(any())).thenReturn(100L);

        PublishProductDTO dto = new PublishProductDTO();
        dto.setTitle("测试"); dto.setPrice(BigDecimal.TEN);

        assertThrows(BizException.class, () ->
            productService.publish(1L, true, dto)
        );
    }

    @Test
    void freeMerchant_shouldAllowWithinLimit() {
        when(productMapper.selectCount(any())).thenReturn(1L);
        when(productMapper.insert(any())).thenReturn(1);

        PublishProductDTO dto = new PublishProductDTO();
        dto.setTitle("测试商品");
        dto.setPrice(new BigDecimal("1000"));
        dto.setTags(java.util.List.of("冰种"));

        assertDoesNotThrow(() -> productService.publish(1L, false, dto));
    }

    @Test
    void deleteProduct_shouldRemoveImagesAndTags() {
        com.gaocui.entity.Product p = new com.gaocui.entity.Product();
        p.setId(1L); p.setMerchantId(1L);
        when(productMapper.selectById(1L)).thenReturn(p);

        productService.delete(1L);

        verify(productMapper).deleteById(1L);
        verify(imageMapper).delete(any());
        verify(tagMapper).delete(any());
        verify(searchService).deleteProduct(1L);
    }
}
