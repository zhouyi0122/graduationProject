package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.dto.OrderDetailDTO;
import com.campus.trade.backend.domain.dto.OrderRefundDTO;
import com.campus.trade.backend.domain.entity.*;
import com.campus.trade.backend.mapper.*;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private com.campus.trade.backend.service.UserService userService;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private OrderReviewMapper orderReviewMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private OrderRefundHistoryMapper orderRefundHistoryMapper;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest createOrderRequest) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("用户未登录，无法创建订单！");
        }
        Long buyerId = userDetails.getId();

        Product product = productMapper.selectById(createOrderRequest.getProductId());
        if (product == null || product.getStatus() != 0) {
            throw new RuntimeException("商品不存在或已售出！");
        }
        if (product.getSellerId().equals(buyerId)) {
            throw new RuntimeException("不能购买自己发布的商品！");
        }

        User buyer = userService.getById(buyerId);
        if (buyer.getBalance().compareTo(product.getPrice()) < 0) {
            throw new RuntimeException("余额不足，请先充值！");
        }
        
        userService.updateBalance(buyerId, product.getPrice().negate(), 1, "购买商品: " + product.getTitle());

        product.setStatus(1);
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNumber(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + buyerId);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setTotalPrice(product.getPrice());
        order.setShippingName(createOrderRequest.getShippingName());
        order.setShippingPhone(createOrderRequest.getShippingPhone());
        order.setShippingAddress(createOrderRequest.getShippingAddress());
        order.setStatus(1); 
        order.setPayTime(new Date());

        this.save(order);
        return order;
    }

    @Override
    public List<Order> getOrders(String type) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("用户未登录！");
        }
        Long currentUserId = userDetails.getId();

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if ("sold".equalsIgnoreCase(type)) {
            queryWrapper.eq("seller_id", currentUserId);
        } else if ("bought".equalsIgnoreCase(type)) {
            queryWrapper.eq("buyer_id", currentUserId);
        }
        queryWrapper.orderByDesc("create_time");

        List<Order> orders = this.list(queryWrapper);
        for (Order order : orders) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductTitle(product.getTitle());
                QueryWrapper<ProductImage> imgWrapper = new QueryWrapper<>();
                imgWrapper.eq("product_id", product.getId()).orderByDesc("is_primary").last("limit 1");
                ProductImage mainImg = productImageMapper.selectOne(imgWrapper);
                order.setImageUrl(mainImg != null ? mainImg.getImageUrl() : "https://picsum.photos/200/200?random=" + product.getId());
            }
            
            // 关联查询维权状态
            OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", order.getId()));
            if (refund != null) {
                order.setDisputeInProgress(refund.getDisputeInProgress());
            } else {
                order.setDisputeInProgress(0);
            }
        }
        return orders;
    }

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在！");

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            QueryWrapper<ProductImage> imgWrapper = new QueryWrapper<>();
            imgWrapper.eq("product_id", product.getId()).orderByDesc("is_primary").last("limit 1");
            ProductImage mainImg = productImageMapper.selectOne(imgWrapper);
            product.setImageUrl(mainImg != null ? mainImg.getImageUrl() : "https://picsum.photos/400/300?random=" + product.getId());
        }

        User buyer = userService.getById(order.getBuyerId());
        User seller = userService.getById(order.getSellerId());
        if (buyer != null) buyer.setPassword(null);
        if (seller != null) seller.setPassword(null);

        List<OrderReview> reviews = orderReviewMapper.selectList(
            new QueryWrapper<OrderReview>().eq("order_id", orderId).orderByAsc("create_time")
        );
        for (OrderReview review : reviews) {
            User reviewer = userService.getById(review.getReviewerId());
            if (reviewer != null) {
                reviewer.setPassword(null);
                review.setReviewer(reviewer);
            }
        }

        // 获取退款信息
        OrderRefundDTO refundInfo = null;
        OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", orderId));
        if (refund != null) {
            List<OrderRefundHistory> history = orderRefundHistoryMapper.selectList(
                new QueryWrapper<OrderRefundHistory>().eq("refund_id", refund.getId()).orderByDesc("create_time")
            );
            refundInfo = new OrderRefundDTO(refund, history);
        }

        return new OrderDetailDTO(order, product, buyer, seller, reviews, refundInfo);
    }

    @Override
    @Transactional
    public void applyRefund(Long orderId, String reason, String description) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在！");
        
        // 只有 1:待发货 或 2:待收货 状态可以申请退款
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("当前状态不支持退款申请！");
        }

        order.setStatus(5); // 5: 退款中
        this.updateById(order);

        // 检查是否已有退款记录（处理撤销后重新申请的情况）
        OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", orderId));
        boolean isReapply = refund != null;

        if (refund == null) {
            refund = new OrderRefund();
            refund.setOrderId(orderId);
        }
        
        refund.setReason(reason);
        refund.setDescription(description);
        refund.setStatus(0); // 0: 退款中
        refund.setDisputeInProgress(0); // 重置维权状态
        
        if (isReapply) {
            orderRefundMapper.updateById(refund);
        } else {
            orderRefundMapper.insert(refund);
        }

        OrderRefundHistory history = new OrderRefundHistory();
        history.setRefundId(refund.getId());
        history.setText(isReapply ? "您重新发起了退款申请，等待卖家处理。" : "您发起了退款申请，等待卖家处理。");
        history.setReason(reason);
        history.setDescription(description);
        history.setIsRejection(0);
        orderRefundHistoryMapper.insert(history);
    }

    @Override
    @Transactional
    public void handleRefund(Long orderId, String action, String reason) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在！");
        if (order.getStatus() != 5) throw new RuntimeException("订单不处于退款处理状态！");

        OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", orderId));
        if (refund == null) throw new RuntimeException("退款记录不存在！");

        OrderRefundHistory history = new OrderRefundHistory();
        history.setRefundId(refund.getId());

        if ("agree".equalsIgnoreCase(action)) {
            // 1. 同意退款逻辑
            order.setStatus(7); // 7: 退款成功
            refund.setStatus(1); // 1: 退款成功
            
            // 2. 资金原路退回
            Product product = productMapper.selectById(order.getProductId());
            String title = product != null ? product.getTitle() : "";
            userService.updateBalance(order.getBuyerId(), order.getTotalPrice(), 0, "退款到账: " + title);
            
            // 3. 商品恢复在售
            if (product != null) {
                product.setStatus(0);
                productMapper.updateById(product);
            }

            history.setText("卖家已同意退款，资金已退回到您的余额。");
        } else {
            // 4. 拒绝退款逻辑
            order.setStatus(8); // 8: 退款失败
            refund.setStatus(2); // 2: 退款失败
            
            history.setText("卖家拒绝了您的退款申请。");
            history.setIsRejection(1);
            history.setReason(reason);
            history.setRejectionTimestamp(System.currentTimeMillis());
        }

        this.updateById(order);
        orderRefundMapper.updateById(refund);
        orderRefundHistoryMapper.insert(history);
    }

    @Override
    @Transactional
    public void applyDispute(Long orderId) {
        OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", orderId));
        if (refund == null) throw new RuntimeException("退款记录不存在！");
        if (refund.getStatus() != 2) throw new RuntimeException("只有在退款被拒绝后才能申请维权！");

        // 1. 标记维权中
        refund.setDisputeInProgress(1);
        orderRefundMapper.updateById(refund);

        // 2. 记录维权历史
        OrderRefundHistory history = new OrderRefundHistory();
        history.setRefundId(refund.getId());
        history.setText("买家发起了维权申请，请等待管理员介入处理。");
        history.setIsRejection(0);
        orderRefundHistoryMapper.insert(history);
    }

    @Override
    @Transactional
    public void adminHandleRefund(Long orderId, String action) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在！");
        
        OrderRefund refund = orderRefundMapper.selectOne(new QueryWrapper<OrderRefund>().eq("order_id", orderId));
        if (refund == null) throw new RuntimeException("退款记录不存在！");

        OrderRefundHistory history = new OrderRefundHistory();
        history.setRefundId(refund.getId());

        if ("force_refund".equalsIgnoreCase(action)) {
            // 1. 管理员强制退款逻辑
            order.setStatus(7); // 7: 退款成功
            refund.setStatus(1); // 1: 退款成功
            refund.setDisputeInProgress(0); // 结束维权
            
            // 资金退回买家
            Product product = productMapper.selectById(order.getProductId());
            String title = product != null ? product.getTitle() : "";
            userService.updateBalance(order.getBuyerId(), order.getTotalPrice(), 0, "管理员介入：强制退款到账 - " + title);
            
            // 商品恢复在售
            if (product != null) {
                product.setStatus(0);
                productMapper.updateById(product);
            }

            history.setText("管理员已介入处理，判定维权成立，执行强制退款。资金已退回到买家余额。");
        } else {
            // 2. 驳回维权逻辑
            order.setStatus(8); // 8: 退款失败
            refund.setStatus(2); // 2: 退款失败
            refund.setDisputeInProgress(2); // 2: 维权结束/驳回
            
            history.setText("管理员已介入处理，判定维权不成立，已驳回维权申请。");
        }

        this.updateById(order);
        orderRefundMapper.updateById(refund);
        orderRefundHistoryMapper.insert(history);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Integer status) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在！");

        order.setStatus(status);
        Date now = new Date();
        if (status == 2) order.setShipTime(now);
        else if (status == 3) {
            order.setCompleteTime(now);
            Product product = productMapper.selectById(order.getProductId());
            String title = product != null ? product.getTitle() : "";
            userService.updateBalance(order.getSellerId(), order.getTotalPrice(), 3, "卖出商品收入: " + title);
        } else if (status == 4) order.setCompleteTime(now);
        else if (status == 6) {
            order.setCancelTime(now);
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                product.setStatus(0);
                productMapper.updateById(product);
            }
        }
        this.updateById(order);
    }
}
