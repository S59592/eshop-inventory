package com.xiaoyi.eshop.inventory.controller;

import com.xiaoyi.eshop.inventory.model.ProductInventory;
import com.xiaoyi.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.xiaoyi.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.xiaoyi.eshop.inventory.request.Request;
import com.xiaoyi.eshop.inventory.service.ProductInventoryService;
import com.xiaoyi.eshop.inventory.service.RequestAsyncProcessService;
import com.xiaoyi.eshop.inventory.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 商品库存Controller
 * @author Dong Jie
 *
 */
@Controller
public class ProductInventoryController {

    private final Logger logger = LoggerFactory.getLogger(ProductInventoryController.class);

    @Resource
    private RequestAsyncProcessService requestAsyncProcessService;
    @Resource
    private ProductInventoryService productInventoryService;

    /**
     * 更新商品库存
     */
    @RequestMapping("/updateProductInventory")
    @ResponseBody
    public Response updateProductInventory(ProductInventory productInventory) {
        logger.info("接收到更新商品库存的请求，商品id= {}, 商品库存数量= {}", productInventory.getProductId(), productInventory.getInventoryCnt());
        Response response = null;
        try {
            Request request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);
            requestAsyncProcessService.process(request);
            response = new Response(Response.SUCCESS);
        } catch (Exception e) {
            logger.error("更新商品库存，错误信息为：{} ", e.getMessage(), e);
            response = new Response(Response.FAILURE);
        }
        return response;
    }

    /**
     * 获取商品库存
     */
    @RequestMapping("/getProductInventory")
    @ResponseBody
    public ProductInventory getProductInventory(Integer productId) {
        logger.info("接收到一个商品库存的读请求，商品id= {}", productId);
        ProductInventory productInventory = null;
        try {
            Request request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, false);
            requestAsyncProcessService.process(request);
            // 将请求扔给service异步去处理以后，就需要while(true)一会儿，在这里hang住
            // 去尝试等待前面有商品库存更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中
            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            long waitTime = 0L;
            while (true) {
                // 一般公司里面，面向用户的读请求控制在200ms就可以了
                if(waitTime > 200) {
                    break;
                }
                // 尝试去redis中读取一次商品库存的缓存数据
                productInventory = productInventoryService.getProductInventoryCache(productId);
                // 如果读取到了结果，那么就返回
                if(productInventory != null) {
                    logger.info("在200ms内读取到了redis中的库存缓存，商品id= {}, 商品库存数量= {}", productInventory.getProductId(), productInventory.getInventoryCnt());
                    return productInventory;
                } else { // 如果没有读取到结果，那么等待一段时间
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime = endTime - startTime;
                }
            }
            // 直接尝试从数据库中读取数据
            productInventory = productInventoryService.findProductInventory(productId);
            if(productInventory != null) {
                // 代码会运行到这里，只有三种情况：
                // 1、就是说，上一次也是读请求，数据刷入了redis，但是redis LRU算法给清理掉了，标志位还是false
                // 所以此时下一个读请求是从缓存中拿不到数据的，再放一个读Request进队列，让数据去刷新一下
                // 2、可能在200ms内，就是读请求在队列中一直积压着，没有等待到它执行（在实际生产环境中，基本是比较坑了）
                // 所以就直接查一次库，然后给队列里塞进去一个刷新缓存的请求
                // 3、数据库里本身就没有，缓存穿透，穿透redis，请求到达mysql库
                request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, true);
                requestAsyncProcessService.process(request);
                return productInventory;
            }
        } catch (Exception e) {
            logger.error("获取商品库存，错误信息为：{} ", e.getMessage(), e);
        }
        return new ProductInventory(productId, -1L);
    }
}
