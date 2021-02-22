package com.xiaoyi.eshop.inventory.request;

import com.xiaoyi.eshop.inventory.model.ProductInventory;
import com.xiaoyi.eshop.inventory.service.ProductInventoryService;

/**
 * 重新加载商品库存的缓存
 * @author Dong Jie
 */
public class ProductInventoryCacheRefreshRequest implements Request {

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 商品库存Service
     */
    private ProductInventoryService productInventoryService;

    public ProductInventoryCacheRefreshRequest(Integer productId,ProductInventoryService productInventoryService) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
    }
    @Override
    public void process() {
        // 从数据库中查询最新的商品库存数量
        ProductInventory productInventory = productInventoryService.findProductInventory(productId);
        // 将最新的商品库存数量，刷新到redis缓存中去
        productInventoryService.setProductInventoryCache(productInventory);
    }
}
